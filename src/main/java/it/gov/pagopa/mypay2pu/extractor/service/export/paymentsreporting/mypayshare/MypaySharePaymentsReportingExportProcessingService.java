package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.config.MyPayPathProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFileNameBuilder;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

@Slf4j
@Service
public class MypaySharePaymentsReportingExportProcessingService {

  private static final String ZIP_VERSION = "1.0";
  private static final String MISSING_XML_DESCRIPTION = "XML file not found on the MyPay share";
  private final PaymentsReportingDao paymentsReportingDao;
  private final ExtractorExportProperties extractorExportProperties;
  private final MyPayPathProperties myPayPathProperties;
  private final CsvService csvService;
  private final ZipFileService zipFileService;

  public MypaySharePaymentsReportingExportProcessingService(PaymentsReportingDao paymentsReportingDao,
                                                             ExtractorExportProperties extractorExportProperties,
                                                             MyPayPathProperties myPayPathProperties,
                                                             CsvService csvService,
                                                             ZipFileService zipFileService) {
    this.paymentsReportingDao = paymentsReportingDao;
    this.extractorExportProperties = extractorExportProperties;
    this.myPayPathProperties = myPayPathProperties;
    this.csvService = csvService;
    this.zipFileService = zipFileService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    List<String> zipFileNames = new ArrayList<>();
    for (String organizationId : request.getIpaCodes()) {
      zipFileNames.addAll(createZips(extractionId, organizationId, request));
    }
    return new ExportFileResult(zipFileNames, null);
  }

  private List<String> createZips(String extractionId, String organizationId, ExtractionRequest request) {
    ExtractionFilters filters = request.getFilters();
    String logicalKey = filters != null ? filters.getLogicalKey() : null;
    OffsetDateTime createdFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime createdTo = filters != null ? filters.getDateTo() : null;
    int pageSize = extractorExportProperties.resolveFileTypeConfiguration(MigrationFileType.PAYMENTS_REPORTING)
      .exportPageSize();
    ExportFileNameBuilder fileNameBuilder = new ExportFileNameBuilder(
      "%s-%s".formatted(extractorExportProperties.brokerIpaCode(), organizationId),
      organizationId,
      true,
      MigrationFileType.PAYMENTS_REPORTING,
      LocalDateTime.now(ZONEID),
      ZIP_VERSION
    );

    log.info(
      "Exporting payments reporting: organizationId={}, logicalKey={}, createdFrom={}, createdTo={}",
      organizationId, logicalKey, createdFrom, createdTo
    );

    int offset = 0;
    int partNumber = 1;
    List<String> zipFileNames = new ArrayList<>();
    while (true) {
      List<Path> records = paymentsReportingDao.findByFilters(
        organizationId,
        request.getLastExtractionDate(),
        createdFrom,
        createdTo,
        logicalKey,
        pageSize,
        offset
      );
      if (records.isEmpty()) {
        break;
      }

      ResolvedFiles resolvedFiles = resolveExistingXmlFiles(records, organizationId);
      Path zipPath = resolveZipPath(extractionId, fileNameBuilder.buildZipPartBaseName(partNumber));
      zipFileService.zipper(zipPath, resolvedFiles.existingXmlFiles(), false);
      zipFileNames.add(zipPath.getFileName().toString());
      writeMissingFilesCsv(
        extractionId,
        fileNameBuilder.buildZipPartBaseName(partNumber),
        resolvedFiles.missingXmlFiles(),
        zipFileNames
      );

      log.info(
        "Generated payments reporting ZIP: organizationId={}, zip={}, processedXmls={}, skippedXmls={}",
        organizationId, zipPath, resolvedFiles.existingXmlFiles().size(), resolvedFiles.missingXmlFiles().size()
      );

      offset += records.size();
      partNumber++;
      if (records.size() < pageSize) {
        break;
      }
    }
    return zipFileNames;
  }

  private ResolvedFiles resolveExistingXmlFiles(List<Path> records, String organizationId) {
    List<Path> xmlFiles = new ArrayList<>();
    List<Path> missingXmlFiles = new ArrayList<>();
    Path baseDirectory = Path.of(myPayPathProperties.directoryRootEnti());
    for (Path filePath : records) {
      Path resolvedPath = baseDirectory.resolve(organizationId).resolve(filePath.getFileName());
      if (Files.exists(resolvedPath)) {
        xmlFiles.add(resolvedPath);
      } else {
        missingXmlFiles.add(filePath);
        log.error("Payments reporting XML file not found: organizationId={}, path={}", organizationId, resolvedPath);
      }
    }
    return new ResolvedFiles(xmlFiles, missingXmlFiles);
  }

  private void writeMissingFilesCsv(String extractionId,
                                    String zipFileBaseName,
                                    List<Path> missingXmlFiles,
                                    List<String> generatedFileNames) {
    if (missingXmlFiles.isEmpty()) {
      return;
    }

    Path discardFilePath = resolveOutputPath(extractionId, zipFileBaseName + ".errors.csv");
    try {
      csvService.createCsv(
        discardFilePath,
        List.<String[]>of(new String[]{"fileName", "description"}),
        missingXmlFiles.stream()
          .map(file -> new String[]{file.getFileName().toString(), MISSING_XML_DESCRIPTION})
          .toList()
      );
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot create payments reporting discard file " + discardFilePath, e);
    }
    generatedFileNames.add(discardFilePath.getFileName().toString());
  }

  private Path resolveZipPath(String extractionId, String zipFileName) {
    return resolveOutputPath(extractionId, zipFileName + ".zip");
  }

  private Path resolveOutputPath(String extractionId, String fileName) {
    Path outputDirectory = Path.of(extractorExportProperties.storagePath()).resolve(extractionId);
    try {
      Files.createDirectories(outputDirectory);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot create payments reporting output directory " + outputDirectory, e);
    }
    return outputDirectory.resolve(fileName);
  }

  private record ResolvedFiles(List<Path> existingXmlFiles, List<Path> missingXmlFiles) {
  }
}
