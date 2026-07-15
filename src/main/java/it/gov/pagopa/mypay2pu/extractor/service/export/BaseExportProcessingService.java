package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.utils.AESUtils;
import jakarta.validation.Validator;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public abstract class BaseExportProcessingService<E, C extends CsvExportDto> {

  private final CsvService csvService;
  private final FileArchiverService fileArchiverService;
  private final Validator validator;
  private final ExtractorExportProperties exportProperties;

  protected BaseExportProcessingService(
    CsvService csvService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    this.csvService = csvService;
    this.fileArchiverService = fileArchiverService;
    this.validator = validator;
    this.exportProperties = exportProperties;
  }

  public final ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    ExtractorExportProperties.FileTypeConfiguration configuration =
      exportProperties.resolveFileTypeConfiguration(getMigrationFileType());
    Path extractionDirectory = Path.of(exportProperties.storagePath()).resolve(extractionId);
    String exportName = getMigrationFileType().name().toLowerCase(Locale.ROOT) + "_" + getZipVersion();
    Path workingDirectory = extractionDirectory.resolve(".work").resolve(exportName);
    Path csvFilePath = workingDirectory.resolve(exportName + ".csv");

    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, csvFilePath)) {
      Supplier<List<C>> pagedRows = new BufferedPageSupplier<>(
        createDataSupplier(request, configuration.exportPageSize()),
        configuration.exportPageSize()
      );
      Supplier<List<C>> validatedRows = new CsvValidatedRowSupplier<>(pagedRows, validator, errorCollector);

      csvService.createCsv(csvFilePath, getDtoClass(), validatedRows, getZipVersion());
      List<Path> filesToArchive = new ArrayList<>();
      filesToArchive.add(csvFilePath);
      errorCollector.writeToFile(csvFilePath).ifPresent(filesToArchive::add);

      Path archivePath = workingDirectory.resolve(exportName + ".zip");
      fileArchiverService.compressAndArchive(filesToArchive, archivePath, extractionDirectory);
      return new ExportFileResult(List.of(resolveArchiveName(extractionDirectory, archivePath)), null);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot generate export for " + getMigrationFileType(), e);
    }
  }

  private Supplier<List<C>> createDataSupplier(ExtractionRequest request, int pageSize) {
    return new Supplier<>() {
      private int offset;

      @Override
      public List<C> get() {
        List<E> entities = retrieveData(request, pageSize, offset);
        if (CollectionUtils.isEmpty(entities)) {
          return List.of();
        }
        offset += entities.size();
        return entities.stream().map(BaseExportProcessingService.this::toExportableEntity).toList();
      }
    };
  }

  private String resolveArchiveName(Path extractionDirectory, Path archivePath) {
    if (Files.exists(extractionDirectory.resolve(archivePath.getFileName()))) {
      return archivePath.getFileName().toString();
    }
    return archivePath.getFileName() + AESUtils.CIPHER_EXTENSION;
  }

  protected abstract MigrationFileType getMigrationFileType();

  protected abstract Class<C> getDtoClass();

  protected abstract String getZipVersion();

  protected abstract C toExportableEntity(E entity);

  protected abstract List<E> retrieveData(ExtractionRequest request, int pageSize, int offset);
}
