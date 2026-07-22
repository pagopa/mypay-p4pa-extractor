package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

/**
 * Base service for CSV export generation and archiving.
 *
 * <p>The export workflow performs the following steps:
 * <ol>
 *   <li>Retrieves paginated data from the underlying source.</li>
 *   <li>Maps each domain entity into a CSV export DTO.</li>
 *   <li>Validates generated records and collects validation errors.</li>
 *   <li>Generates the CSV file.</li>
 *   <li>Archives the generated CSV and any error report into ZIP files.</li>
 *   <li>Cleans up temporary working resources.</li>
 * </ol>
 *
 * <p>Concrete implementations are responsible for retrieving source data and
 * converting domain entities into exportable DTOs.
 *
 * @param <E> source model type retrieved from the data source
 * @param <C> CSV export DTO type
 */
public abstract class BaseExportProcessingService<E extends ExportModel, C extends CsvExportDto> {

  private final CsvService csvService;
  private final CsvPartitionWriterService csvPartitionWriterService;
  private final FileArchiverService fileArchiverService;
  private final Validator validator;
  private final ExtractorExportProperties exportProperties;

  protected BaseExportProcessingService(CsvService csvService,
                                        CsvPartitionWriterService csvPartitionWriterService,
                                        FileArchiverService fileArchiverService,
                                        Validator validator,
                                        ExtractorExportProperties exportProperties) {
    this.csvService = csvService;
    this.csvPartitionWriterService = csvPartitionWriterService;
    this.fileArchiverService = fileArchiverService;
    this.validator = validator;
    this.exportProperties = exportProperties;
  }

  /**
   * Executes the complete export process for the provided extraction request.
   *
   * <p>The generated CSV file is validated, archived into a ZIP file and stored
   * in the configured extraction directory. If row validation errors are found,
   * an additional ZIP archive containing the error report is generated.
   *
   * @param extractionId unique identifier of the extraction execution
   * @param request extraction request containing filtering criteria
   * @return information about the generated archive files
   * @throws IllegalStateException if the export generation or file handling fails
   */
  public final ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    ExtractorExportProperties.FileTypeConfiguration configuration =
      exportProperties.resolveFileTypeConfiguration(getMigrationFileType());

    Path extractionDirectory = Path.of(exportProperties.storagePath())
      .resolve(extractionId);

    Path workingDirectory = Path.of(exportProperties.tempBaseDir())
      .resolve(extractionId)
      .resolve(getMigrationFileType().name().toLowerCase(Locale.ROOT));

    ExportFileNameBuilder fileNameBuilder = new ExportFileNameBuilder(
      exportProperties.brokerIpaCode(),
      getMigrationFileType(),
      LocalDateTime.now(ZONEID),
      getZipVersion()
    );

    Path baseCsvFilePath = workingDirectory.resolve(fileNameBuilder.buildCsvFileName());

    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, baseCsvFilePath)) {
      Supplier<List<C>> rowsSupplier = buildRowsSupplier(
        request,
        configuration.exportPageSize(),
        errorCollector
      );

      List<Path> csvFilePaths = csvPartitionWriterService.writeCsv(
        workingDirectory,
        fileNameBuilder,
        getDtoClass(),
        rowsSupplier,
        getZipVersion(),
        configuration.exportPageSize()
      );

      Optional<Path> errorFilePath =
        errorCollector.writeToFile(baseCsvFilePath);

      List<String> archivedFiles = archiveExportFiles(
        csvFilePaths,
        errorFilePath,
        fileNameBuilder.buildBaseName(),
        extractionDirectory,
        workingDirectory
      );

      return new ExportFileResult(archivedFiles, null);
    } catch (IOException e) {
      throw new IllegalStateException(
        "Cannot generate export for " + getMigrationFileType(),
        e
      );
    } finally {
      cleanupWorkingDirectory(workingDirectory);
    }
  }

  /**
   * Builds the supplier chain used to retrieve, transform and validate export rows.
   *
   * @param request extraction request
   * @param pageSize page size used for paginated retrieval
   * @param errorCollector collector used to store row validation errors
   * @return supplier providing validated export rows
   */
  private Supplier<List<C>> buildRowsSupplier(ExtractionRequest request,
                                              int pageSize,
                                              CsvRowErrorCollector errorCollector) {
    Supplier<List<C>> exportRowsSupplier = new PaginatedExportRowsSupplier<>(
      (limit, offset) -> retrieveData(request, limit, offset),
      this::toExportableEntity,
      pageSize
    );
    return new CsvValidatedRowSupplier<>(exportRowsSupplier, validator, errorCollector);
  }

  /**
   * Archives generated export artifacts into ZIP files.
   *
   * <p>The main CSV is always archived. If an error CSV is present, it is archived
   * separately and included in the returned file list.
   *
   * @param csvFilePaths generated export CSVs
   * @param errorFilePath optional error CSV
   * @param exportName export base name
   * @param extractionDirectory target archive directory
   * @param workingDirectory temporary working directory
   * @return names of archived files
   * @throws IOException if archive creation fails
   */
  private List<String> archiveExportFiles(List<Path> csvFilePaths,
                                          Optional<Path> errorFilePath,
                                          String exportName,
                                          Path extractionDirectory,
                                          Path workingDirectory) throws IOException {
    List<String> archivedFileNames = new ArrayList<>(2);

    Path exportZipPath = workingDirectory.resolve(exportName + ".zip");
    fileArchiverService.compressAndArchive(csvFilePaths, exportZipPath, extractionDirectory);
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

  /**
   * Removes the temporary working directory used during export generation.
   *
   * @param workingDirectory directory to delete
   * @throws IllegalStateException if the directory cannot be removed
   */
  private void cleanupWorkingDirectory(Path workingDirectory) {
    if (workingDirectory == null || !Files.exists(workingDirectory)) {
      return;
    }
    boolean deleted = FileSystemUtils.deleteRecursively(workingDirectory.toFile());
    if (!deleted && Files.exists(workingDirectory)) {
      throw new IllegalStateException("Cannot clean temporary export directory " + workingDirectory);
    }
  }

  /**
   * Returns the migration file type managed by the implementation.
   *
   * @return migration file type
   */
  protected abstract MigrationFileType getMigrationFileType();

  /**
   * Returns the DTO class used for CSV generation.
   *
   * @return export DTO class
   */
  protected abstract Class<C> getDtoClass();

  /**
   * Returns the ZIP version identifier to be included in generated file names.
   *
   * @return ZIP version
   */
  protected abstract String getZipVersion();

  /**
   ** Converts a source domain model into its exportable CSV representation.
   *
   * @param model source model
   * @return export DTO
   */
  protected abstract C toExportableEntity(E model);

  /**
   * Retrieves a page of source data to be exported.
   *
   * @param request extraction request
   * @param pageSize maximum number of records to retrieve
   * @param offset starting offset for pagination
   * @return list of retrieved records
   */
  protected abstract List<E> retrieveData(ExtractionRequest request, int pageSize, int offset);
}
