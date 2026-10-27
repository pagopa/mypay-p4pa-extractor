package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypivot;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentReportingMyPivotDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting.FlussoRiversamentoMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFileNameBuilder;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.service.xml.FlussoRiversamentoXmlMarshallerService;
import it.gov.pagopa.mypay2pu.extractor.validation.ValueLogicalKeyValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

@Slf4j
@Service
public class MypivotPaymentsReportingExportProcessingService {

  private static final String ZIP_VERSION = "1.0";
  private static final String IPA_CODE_FILE_NAME_FORMAT = "%s-%s";
  private static final int MAX_IO_ATTEMPTS = 3;
  private static final long IO_RETRY_DELAY_MILLIS = 100L;

  private final PaymentReportingMyPivotDao paymentReportingMyPivotDao;
  private final FlussoRiversamentoMapper flussoRiversamentoMapper;
  private final FlussoRiversamentoXmlMarshallerService xmlMarshallerService;
  private final ExtractorExportProperties extractorExportProperties;
  private final CsvService csvService;
  private final ZipFileService zipFileService;

  public MypivotPaymentsReportingExportProcessingService(
    PaymentReportingMyPivotDao paymentReportingMyPivotDao,
    FlussoRiversamentoMapper flussoRiversamentoMapper,
    FlussoRiversamentoXmlMarshallerService xmlMarshallerService,
    ExtractorExportProperties extractorExportProperties,
    CsvService csvService,
    ZipFileService zipFileService
  ) {
    this.paymentReportingMyPivotDao = paymentReportingMyPivotDao;
    this.flussoRiversamentoMapper = flussoRiversamentoMapper;
    this.xmlMarshallerService = xmlMarshallerService;
    this.extractorExportProperties = extractorExportProperties;
    this.csvService = csvService;
    this.zipFileService = zipFileService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    List<ArchiveFile> archiveFiles = new ArrayList<>();
    for (String ipaCode : request.getIpaCodes()) {
      archiveFiles.addAll(createZips(extractionId, ipaCode, request));
    }
    return new ExportFileResult(archiveFiles, null);
  }

  private List<ArchiveFile> createZips(String extractionId, String ipaCode, ExtractionRequest request) {
    ExtractionFilters filters = request.getFilters();
    List<String> flowIdentifiers = ValueLogicalKeyValidator.parseLogicalKey(
      filters != null ? filters.getLogicalKey() : null
    );
    OffsetDateTime createdFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime createdTo = filters != null ? filters.getDateTo() : null;
    int pageSize = extractorExportProperties.resolveFileTypeConfiguration(MigrationFileType.PAYMENTS_REPORTING_MYPIVOT)
      .exportPageSize();
    ExportFileNameBuilder fileNameBuilder = new ExportFileNameBuilder(
      IPA_CODE_FILE_NAME_FORMAT.formatted(extractorExportProperties.brokerIpaCode(), ipaCode),
      ipaCode,
      true,
      MigrationFileType.PAYMENTS_REPORTING,
      LocalDateTime.now(ZONEID),
      ZIP_VERSION
    );

    int offset = 0;
    int partNumber = 1;
    List<ArchiveFile> archiveFiles = new ArrayList<>();
    List<Path> errorFilePaths = new ArrayList<>();
    List<String> pageFlowIdentifiers;
    do {
      pageFlowIdentifiers = paymentReportingMyPivotDao.findFlowIdentifiers(
        ipaCode, request.getLastExtractionDate(), createdFrom, createdTo, flowIdentifiers, pageSize, offset
      );
      if (pageFlowIdentifiers.isEmpty()) {
        break;
      }

      List<Path> xmlFiles = new ArrayList<>();
      List<Failure> failures = new ArrayList<>();
      for (String flowIdentifier : pageFlowIdentifiers) {
        createXml(
          extractionId, ipaCode, flowIdentifier, request.getLastExtractionDate(), createdFrom, createdTo, failures
        ).ifPresent(xmlFiles::add);
      }
      Path zipPath = resolveOutputPath(extractionId, fileNameBuilder.buildZipPartBaseName(partNumber) + ".zip");
      zip(
        zipPath,
        xmlFiles,
        true,
        file -> IPA_CODE_FILE_NAME_FORMAT.formatted(ipaCode, file.getFileName())
      );
      archiveFiles.add(new ArchiveFile()
        .name(zipPath.getFileName().toString())
        .files(xmlFiles.stream().map(file -> IPA_CODE_FILE_NAME_FORMAT.formatted(ipaCode, file.getFileName())).toList()));
      writeFailuresCsv(extractionId, fileNameBuilder.buildZipPartBaseName(partNumber), failures).ifPresent(errorFilePaths::add);
      offset += pageFlowIdentifiers.size();
      partNumber++;
    } while (pageFlowIdentifiers.size() >= pageSize);

    if (!errorFilePaths.isEmpty()) {
      Path errorZipPath = resolveOutputPath(extractionId, fileNameBuilder.buildZipBaseName() + ".errors.zip");
      zip(errorZipPath, errorFilePaths, true, file -> file.getFileName().toString());
      archiveFiles.add(new ArchiveFile()
        .name(errorZipPath.getFileName().toString())
        .files(errorFilePaths.stream().map(path -> path.getFileName().toString()).toList()));
    }
    return archiveFiles;
  }

  private Optional<Path> createXml(
    String extractionId,
    String ipaCode,
    String flowIdentifier,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime createdFrom,
    OffsetDateTime createdTo,
    List<Failure> failures
  ) {
    try {
      List<PaymentReporting> rows = paymentReportingMyPivotDao.findRowsByFlowIdentifier(
        ipaCode, flowIdentifier, lastExtractionDate, createdFrom, createdTo
      );
      Path xmlPath = resolveOutputPath(extractionId, flowIdentifier + ".xml");
      marshal(flussoRiversamentoMapper.map(rows), xmlPath);
      return Optional.of(xmlPath);
    } catch (IllegalArgumentException | IllegalStateException exception) {
      log.error("Cannot generate payments reporting XML: ipaCode={}, flowIdentifier={}", ipaCode, flowIdentifier, exception);
      failures.add(new Failure(flowIdentifier, exception.getMessage()));
      return Optional.empty();
    }
  }

  private Optional<Path> writeFailuresCsv(
    String extractionId, String zipFileBaseName, List<Failure> failures
  ) {
    if (failures.isEmpty()) {
      return Optional.empty();
    }
    Path errorFile = resolveOutputPath(extractionId, zipFileBaseName + ".errors.csv");
    try {
      executeWithIoRetry(
        "create payments reporting discard file " + errorFile,
        () -> csvService.createCsv(
          errorFile,
          List.<String[]>of(new String[]{"flowIdentifier", "description"}),
          failures.stream().map(failure -> new String[]{failure.flowIdentifier(), failure.description()}).toList()
        )
      );
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting discard file " + errorFile, exception);
    }
    return Optional.of(errorFile);
  }

  private void marshal(it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento flow, Path xmlPath) {
    try {
      executeWithIoRetry(
        "create payments reporting XML " + xmlPath,
        () -> xmlMarshallerService.marshal(flow, xmlPath)
      );
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting XML " + xmlPath, exception);
    }
  }

  private void zip(
    Path zipPath,
    List<Path> files,
    boolean deleteSourceFiles,
    java.util.function.Function<Path, String> entryNameResolver
  ) {
    try {
      executeWithIoRetry(
        "create payments reporting ZIP " + zipPath,
        () -> zipFileService.zipper(zipPath, files, deleteSourceFiles, entryNameResolver)
      );
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting ZIP " + zipPath, exception);
    }
  }

  private void executeWithIoRetry(String operationDescription, IoOperation operation) throws IOException {
    for (int attempt = 1; attempt <= MAX_IO_ATTEMPTS; attempt++) {
      try {
        operation.execute();
        return;
      } catch (IOException exception) {
        if (attempt == MAX_IO_ATTEMPTS) {
          throw exception;
        }
        waitBeforeRetry(operationDescription, attempt, exception);
      } catch (IllegalStateException exception) {
        IOException ioException = findIoException(exception);
        if (ioException == null) {
          throw exception;
        }
        if (attempt == MAX_IO_ATTEMPTS) {
          throw ioException;
        }
        waitBeforeRetry(operationDescription, attempt, ioException);
      }
    }
  }

  private IOException findIoException(Throwable exception) {
    Throwable cause = exception.getCause();
    while (cause != null) {
      if (cause instanceof IOException ioException) {
        return ioException;
      }
      cause = cause.getCause();
    }
    return null;
  }

  private void waitBeforeRetry(String operationDescription, int attempt, IOException exception) {
    log.warn(
      "I/O failure while attempting to {} (attempt {}/{}), retrying",
      operationDescription,
      attempt,
      MAX_IO_ATTEMPTS,
      exception
    );
    try {
      Thread.sleep(IO_RETRY_DELAY_MILLIS * attempt);
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while retrying to " + operationDescription, interruptedException);
    }
  }

  private Path resolveOutputPath(String extractionId, String fileName) {
    Path outputDirectory = Path.of(extractorExportProperties.storagePath()).resolve(extractionId).normalize();
    try {
      Files.createDirectories(outputDirectory);
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting output directory " + outputDirectory, exception);
    }
    Path outputPath = outputDirectory.resolve(fileName).normalize();
    if (!outputPath.startsWith(outputDirectory)) {
      throw new IllegalArgumentException("Invalid payments reporting output file name: " + fileName);
    }
    return outputPath;
  }

  private record Failure(String flowIdentifier, String description) {
  }

  @FunctionalInterface
  private interface IoOperation {
    void execute() throws IOException;
  }
}
