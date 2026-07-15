package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.utils.FileUtils;
import jakarta.validation.Validator;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

public abstract class BaseExportProcessingService<M, C extends CsvExportDto> {

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
    Path workingDirectory = FileUtils.createWorkingDirectory(extractionDirectory, getMigrationFileType().name().toLowerCase(Locale.ROOT));

    String exportName = "%s-%s-%s-%s".formatted(
      exportProperties.brokerIpaCode(),
      getMigrationFileType().name(),
      LocalDateTime.now(ZONEID).format(FileUtils.FILE_TIMESTAMP_FORMATTER),
      getZipVersion()
    );
    Path csvFilePath = workingDirectory.resolve(exportName + ".csv");

    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, csvFilePath)) {
      Supplier<List<C>> exportRowsSupplier = buildExportRowsSupplier(request, configuration.exportPageSize());
      Supplier<List<C>> validatedRowsSupplier = new CsvValidatedRowSupplier<>(exportRowsSupplier, validator, errorCollector);
      Supplier<List<C>> bufferedRowsSupplier = new BufferedPageSupplier<>(validatedRowsSupplier, configuration.exportPageSize());

      // TODO follow-up: split large exports into multiple CSV parts instead of a single archive.
      csvService.createCsv(csvFilePath, getDtoClass(), bufferedRowsSupplier, getZipVersion());

      errorCollector.writeToFile(csvFilePath);
      Path archivePath = workingDirectory.resolve(exportName + ".zip");
      fileArchiverService.compressAndArchive(List.of(csvFilePath), archivePath, extractionDirectory);
      return new ExportFileResult(List.of(archivePath.getFileName().toString()), null);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot generate export for " + getMigrationFileType(), e);
    } finally {
      FileUtils.deleteRecursively(workingDirectory);
    }
  }

  private Supplier<List<C>> buildExportRowsSupplier(ExtractionRequest request, int pageSize) {
    return new PaginatedExportRowsSupplier<>(
      (limit, offset) -> retrieveData(request, limit, offset),
      this::toExportableEntity,
      pageSize
    );
  }

  protected abstract MigrationFileType getMigrationFileType();

  protected abstract Class<C> getDtoClass();

  protected abstract String getZipVersion();

  protected abstract C toExportableEntity(M model);

  protected abstract List<M> retrieveData(ExtractionRequest request, int pageSize, int offset);
}
