package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

public abstract class BaseExportProcessingService<M, C extends CsvExportDto> {

  private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final CsvService csvService;
  private final FileArchiverService fileArchiverService;
  private final Validator validator;
  private final ExtractorExportProperties exportProperties;

  protected BaseExportProcessingService(CsvService csvService,
                                        FileArchiverService fileArchiverService,
                                        Validator validator,
                                        ExtractorExportProperties exportProperties) {
    this.csvService = csvService;
    this.fileArchiverService = fileArchiverService;
    this.validator = validator;
    this.exportProperties = exportProperties;
  }

  public final ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    ExtractorExportProperties.FileTypeConfiguration configuration =
      exportProperties.resolveFileTypeConfiguration(getMigrationFileType());
    Path extractionDirectory = Path.of(exportProperties.storagePath()).resolve(extractionId);
    Path workingDirectory = Path.of(exportProperties.tempBaseDir()).resolve(extractionId)
      .resolve(getMigrationFileType().name().toLowerCase(Locale.ROOT));

    String exportName = "%s-%s-%s-%s".formatted(
      exportProperties.brokerIpaCode(),
      getMigrationFileType().name(),
      LocalDateTime.now(ZONEID).format(FILE_TIMESTAMP_FORMATTER),
      getZipVersion()
    );
    Path csvFilePath = workingDirectory.resolve(exportName + ".csv");

    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, csvFilePath)) {
      Supplier<List<C>> decoratedRowsSupplier = buildDecoratedRowsSupplier(request, configuration.exportPageSize(), errorCollector);

      // TODO follow-up: P4ADEV-4905 split large exports into multiple CSV parts instead of a single archive.
      csvService.createCsv(csvFilePath, getDtoClass(), decoratedRowsSupplier, getZipVersion());

      Optional<Path> errorFilePath = errorCollector.writeToFile(csvFilePath);
      List<String> archivedFiles = archiveExportFiles(csvFilePath, errorFilePath, exportName, extractionDirectory, workingDirectory);
      return new ExportFileResult(archivedFiles, null);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot generate export for " + getMigrationFileType(), e);
    } finally {
      cleanupWorkingDirectory(workingDirectory);
    }
  }

  private Supplier<List<C>> buildDecoratedRowsSupplier(ExtractionRequest request,
                                                       int pageSize,
                                                       CsvRowErrorCollector errorCollector) {
    Supplier<List<C>> exportRowsSupplier = new PaginatedExportRowsSupplier<>(
      (limit, offset) -> retrieveData(request, limit, offset),
      this::toExportableEntity,
      pageSize
    );
    Supplier<List<C>> validatedRowsSupplier = new CsvValidatedRowSupplier<>(exportRowsSupplier, validator, errorCollector);
    return new BufferedPageSupplier<>(validatedRowsSupplier, pageSize);
  }

  private List<String> archiveExportFiles(Path csvFilePath,
                                          Optional<Path> errorFilePath,
                                          String exportName,
                                          Path extractionDirectory,
                                          Path workingDirectory) throws IOException {
    List<String> archivedFileNames = new ArrayList<>(2);

    Path exportZipPath = workingDirectory.resolve(exportName + ".zip");
    fileArchiverService.compressAndArchive(List.of(csvFilePath), exportZipPath, extractionDirectory);
    archivedFileNames.add(exportZipPath.getFileName().toString());

    if (errorFilePath.isPresent()) {
      Path errorCsvPath = errorFilePath.get();
      String errorZipName = errorCsvPath.getFileName().toString().replace(".csv", ".zip");
      Path errorZipPath = workingDirectory.resolve(errorZipName);
      fileArchiverService.compressAndArchive(List.of(errorCsvPath), errorZipPath, extractionDirectory);
      archivedFileNames.add(errorZipPath.getFileName().toString());
    }
    return archivedFileNames;
  }

  private void cleanupWorkingDirectory(Path workingDirectory) {
    if (workingDirectory == null || !Files.exists(workingDirectory)) {
      return;
    }
    boolean deleted = FileSystemUtils.deleteRecursively(workingDirectory.toFile());
    if (!deleted && Files.exists(workingDirectory)) {
      throw new IllegalStateException("Cannot clean temporary export directory " + workingDirectory);
    }
  }

  protected abstract MigrationFileType getMigrationFileType();

  protected abstract Class<C> getDtoClass();

  protected abstract String getZipVersion();

  protected abstract C toExportableEntity(M model);

  protected abstract List<M> retrieveData(ExtractionRequest request, int pageSize, int offset);
}
