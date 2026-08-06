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
 *   <li>Runs one or more single export generations according to subclass strategy.</li>
 *   <li>Retrieves paginated data from the underlying source.</li>
 *   <li>Maps each domain entity into a CSV export DTO.</li>
 *   <li>Validates generated records and collects validation errors.</li>
 *   <li>Generates one or more CSV files (including partitioned files).</li>
 *   <li>Archives all generated CSV files into one final ZIP and, when present, all error CSV files into one errors ZIP.</li>
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
   * <p>Single-run versus multi-run behaviour is delegated to
   * {@link #executeExport(ExtractionRequest, Path, int, List, List)} so subclasses can customize
   * the splitting strategy (for example by IPA code) while preserving the common archiving workflow.
   *
   * <p>All produced CSV files are archived in one final export ZIP. When validation errors are present,
   * all produced error CSV files are archived in one final errors ZIP.
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
    ExportFileNameBuilder zipFileNameBuilder = new ExportFileNameBuilder(
      exportProperties.brokerIpaCode(),
      resolveOrganizationIpaCode(request),
      useBrokerIpaAsPrefix(),
      getMigrationFileType(),
      LocalDateTime.now(ZONEID),
      getZipVersion()
    );

    try {
      List<Path> csvFilePaths = new ArrayList<>();
      List<Path> errorFilePaths = new ArrayList<>();

      executeExport(
        request,
        workingDirectory,
        configuration.exportPageSize(),
        csvFilePaths,
        errorFilePaths
      );

      List<String> archivedFiles = archiveExportFiles(
        csvFilePaths,
        errorFilePaths,
        zipFileNameBuilder.buildZipBaseName(),
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
   * Executes one physical export generation for the provided request.
   *
   * <p>This method preserves the existing single-export behavior: row supply, mapping, validation,
   * CSV partitioning and optional error CSV creation. It returns all generated CSV paths for this run
   * and the optional error CSV path generated by the {@link CsvRowErrorCollector}.
   *
   * @param request extraction request used for this single run
   * @param workingDirectory temporary directory used to generate artifacts
   * @param pageSize pagination and partitioning size for row retrieval/writing
   * @return generated CSV paths and optional error CSV path for this single run
   * @throws IOException if CSV writing or error report generation fails
   */
  private ExportGenerationResult executeSingleExport(ExtractionRequest request,
                                                     Path workingDirectory,
                                                     int pageSize) throws IOException {
    ExportFileNameBuilder fileNameBuilder = new ExportFileNameBuilder(
      exportProperties.brokerIpaCode(),
      resolveOrganizationIpaCode(request),
      useBrokerIpaAsPrefix(),
      getMigrationFileType(),
      LocalDateTime.now(ZONEID),
      getZipVersion()
    );

    Path baseCsvFilePath = workingDirectory.resolve(fileNameBuilder.buildCsvFileName());
    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, baseCsvFilePath)) {
      Supplier<List<C>> rowsSupplier = buildRowsSupplier(
        request,
        pageSize,
        errorCollector
      );

      List<Path> csvFilePaths = csvPartitionWriterService.writeCsv(
        workingDirectory,
        fileNameBuilder,
        getDtoClass(),
        rowsSupplier,
        getZipVersion(),
        pageSize
      );
      Optional<Path> errorFilePath = errorCollector.writeToFile(baseCsvFilePath);
      return new ExportGenerationResult(csvFilePaths, errorFilePath);
    }
  }

  /**
   * Executes export runs for the provided request and appends generated files into collectors.
   *
   * <p>Default behaviour executes a single run using the full request. Subclasses can override
   * this method to implement split strategies while still reusing
   * {@link #executeSingleExport(ExtractionRequest, Path, int)}.
   *
   * @param request extraction request to export
   * @param workingDirectory temporary directory used to generate artifacts
   * @param pageSize pagination and partitioning size for row retrieval/writing
   * @param csvFilePaths collector for generated export CSV file paths
   * @param errorFilePaths collector for generated error CSV file paths
   * @throws IOException if CSV writing or error report generation fails
   */
  protected void executeExport(ExtractionRequest request,
                               Path workingDirectory,
                               int pageSize,
                               List<Path> csvFilePaths,
                               List<Path> errorFilePaths) throws IOException {
    ExportGenerationResult result = executeSingleExport(
      request,
      workingDirectory,
      pageSize
    );
    csvFilePaths.addAll(result.csvFiles());
    result.errorFile().ifPresent(errorFilePaths::add);
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
    Supplier<List<E>> sourceRowsSupplier = new PaginatedExportRowsSupplier<>(
      (limit, offset) -> retrieveData(request, limit, offset),
      pageSize
    );
    return new CsvValidatedRowSupplier<>(
      sourceRowsSupplier,
      this::toExportableEntity,
      validator,
      errorCollector
    );
  }

  /**
   * Archives generated export artifacts into ZIP files.
   *
   * <p>The main export ZIP always contains all generated CSV files. If one or more
   * error CSV files are present, they are archived together into a separate
   * {@code .errors.zip} file.
   *
   * @param csvFilePaths generated export CSVs
   * @param errorFilePaths generated error CSVs (possibly empty)
   * @param exportName export base name
   * @param extractionDirectory target archive directory
   * @param workingDirectory temporary working directory
   * @return names of archived files
   * @throws IOException if archive creation fails
   */
  private List<String> archiveExportFiles(List<Path> csvFilePaths,
                                          List<Path> errorFilePaths,
                                          String exportName,
                                          Path extractionDirectory,
                                          Path workingDirectory) throws IOException {
    List<String> archivedFileNames = new ArrayList<>(2);

    Path exportZipPath = workingDirectory.resolve(exportName + ".zip");
    fileArchiverService.compressAndArchive(csvFilePaths, exportZipPath, extractionDirectory);
    archivedFileNames.add(exportZipPath.getFileName().toString());

    if (!errorFilePaths.isEmpty()) {
      String errorZipName = exportName + ".errors.zip";
      Path errorZipPath = workingDirectory.resolve(errorZipName);
      fileArchiverService.compressAndArchive(errorFilePaths, errorZipPath, extractionDirectory);
      archivedFileNames.add(errorZipPath.getFileName().toString());
    }
    return archivedFileNames;
  }

  private String resolveOrganizationIpaCode(ExtractionRequest request) {
    return request.getIpaCodes().getFirst();
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
   * Determines whether CSV filenames use the broker IPA code instead of the organization IPA code.
   *
   * @return {@code true} when the broker IPA code must be used as CSV filename prefix
   */
  protected boolean useBrokerIpaAsPrefix() {
    return true;
  }

  /**
   * Converts a source domain model into its exportable CSV representation.
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

  private record ExportGenerationResult(
    List<Path> csvFiles,
    Optional<Path> errorFile
  ) {
  }
}
