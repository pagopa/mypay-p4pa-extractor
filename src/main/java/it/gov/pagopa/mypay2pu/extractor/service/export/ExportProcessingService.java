package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.enums.ExportFileVersion;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.utils.Constants;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Abstract base class for processing and exporting data in batches.
 * <p>
 * This service orchestrates the extraction, transformation, and export of data into CSV files.
 * It handles partitioning of large datasets and manages the creation of export files with
 * appropriate naming and versioning.
 * <p>
 * Type parameters:
 * <ul>
 *   <li>{@code I} - Input data type retrieved from the data source</li>
 *   <li>{@code O} - Output data type written to the export file</li>
 * </ul>
 *
 * @param <I> the input entity type
 * @param <O> the output exportable entity type
 */
public abstract class ExportProcessingService<I, O> {
  private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final ExtractorExportProperties extractorExportProperties;
  private final ExportBatchCoordinator exportBatchCoordinator;
  private final ExportFilePartWriter exportFilePartWriter;
  private final ExportFileStatusDao exportFileStatusDao;

  /**
   * Constructs the export processing service with required dependencies.
   *
   * @param extractorExportProperties configuration for export behavior
   * @param exportBatchCoordinator coordinates batch operations and data retrieval
   * @param exportFilePartWriter writes export data to files
   * @param exportFileStatusDao manages export directory resolution
   */
  protected ExportProcessingService(ExtractorExportProperties extractorExportProperties,
                                    ExportBatchCoordinator exportBatchCoordinator,
                                    ExportFilePartWriter exportFilePartWriter,
                                    ExportFileStatusDao exportFileStatusDao) {
    this.extractorExportProperties = extractorExportProperties;
    this.exportBatchCoordinator = exportBatchCoordinator;
    this.exportFilePartWriter = exportFilePartWriter;
    this.exportFileStatusDao = exportFileStatusDao;
  }

  /**
   * Extracts and exports data based on the provided extraction request.
   * <p>
   * This method orchestrates the complete export workflow:
   * <ol>
   *   <li>Builds the execution context with sizing and configuration</li>
   *   <li>Retrieves and buffers source data</li>
   *   <li>Inspects the batch to determine partitioning strategy</li>
   *   <li>Iterates through data parts, transforming and writing to files</li>
   * </ol>
   *
   * @param extractionId unique identifier for this extraction
   * @param request the extraction request containing filtering and configuration
   * @return export file result containing generated file names and any error file names
   */
  public ExportFileResult extract(String extractionId, ExtractionRequest request) {
    ExportExecutionContext executionContext = buildExecutionContext(
      extractionId,
      getMigrationFileType(),
      getAvgRowSize()
    );
    BufferedBatchSource<I> bufferedRows = exportBatchCoordinator.createBufferedSource(
      retrieveDataSupplier(request, executionContext)
    );
    ExportBatchInspection batchInspection = exportBatchCoordinator.inspect(
      bufferedRows,
      executionContext.maxRowsPerPart()
    );

    List<String> generatedFiles = new ArrayList<>();
    List<String> generatedErrorFiles = new ArrayList<>();

    if (!batchInspection.hasData()) {
      collectPartResult(
        writeExportPart(executionContext, null, List::of),
        generatedFiles,
        generatedErrorFiles
      );
      return new ExportFileResult(generatedFiles, generatedErrorFiles, null);
    }

    int part = 1;
    while (bufferedRows.hasMoreData()) {
      Integer partNumber = batchInspection.multipart() ? part : null;
      Supplier<List<O>> csvRowsSupplier = exportBatchCoordinator.createMappedPartSupplier(
        bufferedRows,
        executionContext.maxRowsPerPart(),
        executionContext.pageSize(),
        this::toExportableEntity
      );
      collectPartResult(
        writeExportPart(executionContext, partNumber, csvRowsSupplier),
        generatedFiles,
        generatedErrorFiles
      );
      part++;
    }

    return new ExportFileResult(generatedFiles, generatedErrorFiles, null);
  }

  /**
   * Creates a supplier that provides all data in a single batch.
   *
   * @param data the data list to supply
   * @return a supplier that returns the data list
   */
  protected final Supplier<List<I>> createSingleBatchSupplier(List<I> data) {
    return exportBatchCoordinator.createSingleBatchSupplier(data);
  }

  /**
   * Creates a supplier that retrieves data in pages.
   * <p>
   * The supplier invokes the pageRetriever function with incrementing page numbers
   * until it returns an empty list, indicating all pages have been retrieved.
   *
   * @param pageSize the number of items per page
   * @param pageRetriever function to retrieve a specific page of data
   * @return a supplier that iterates through all pages
   */
  protected final Supplier<List<I>> createPagedSupplier(
    int pageSize,
    IntFunction<List<I>> pageRetriever
  ) {
    return exportBatchCoordinator.createPagedSupplier(pageSize, pageRetriever);
  }

  /**
   * Builds the supplier used to retrieve source data for the export.
   * <p>
   * Default implementation loads all data in a single batch.
   * Subclasses can override this to provide paged retrieval based on the
   * current export execution context, such as page size or part sizing.
   *
   * @param request the extraction request
   * @param executionContext runtime export settings, available for subclasses
   * @return a supplier producing source batches
   */
  protected Supplier<List<I>> retrieveDataSupplier(ExtractionRequest request,
                                                   ExportExecutionContext executionContext) {
    return createSingleBatchSupplier(retrieveData(request));
  }

  /**
   * Writes a part of the export data to file.
   * <p>
   * Delegates to the file part writer to create the export file with the appropriate name
   * and format, using the supplied CSV rows.
   *
   * @param executionContext the current export execution context
   * @param partNumber the part number for multipart exports, or null for single-part
   * @param csvRowsSupplier supplier providing the rows to write
   * @return the result of the write operation, including file name and error file name if any
   */
  private ExportPartResult writeExportPart(ExportExecutionContext executionContext,
                                           Integer partNumber,
                                           Supplier<List<O>> csvRowsSupplier) {
    return exportFilePartWriter.writePart(
      executionContext,
      executionContext.buildExportBaseFileName(partNumber),
      getDtoClass(),
      csvRowsSupplier
    );
  }

  /**
   * Collects the result of a written export part into the result lists.
   * <p>
   * Adds the generated file name to the files list and, if present, adds the error file name
   * to the error files list.
   *
   * @param partResult the result from writing a part
   * @param generatedFiles list to accumulate generated file names
   * @param generatedErrorFiles list to accumulate generated error file names
   */
  private void collectPartResult(ExportPartResult partResult,
                                 List<String> generatedFiles,
                                 List<String> generatedErrorFiles) {
    generatedFiles.add(partResult.fileName());
    partResult.errorFileName().ifPresent(generatedErrorFiles::add);
  }

  /**
   * Builds the execution context for the current export.
   * <p>
   * The context contains:
   * <ul>
   *   <li>The extraction directory for file storage</li>
   *   <li>Migration file type information</li>
   *   <li>Broker certification number for ZIP naming</li>
   *   <li>ZIP version for versioning</li>
   *   <li>Extraction timestamp</li>
   *   <li>Maximum rows per file part (calculated from file size and avg row size)</li>
   *   <li>Page size for paged data retrieval</li>
   * </ul>
   *
   * @param extractionId unique identifier for the extraction
   * @param migrationFileType the type of migration file being exported
   * @param avgRowSize the average size in bytes of a row
   * @return the execution context for this export
   * @throws IllegalStateException if required broker certification configuration is missing
   */
  public ExportExecutionContext buildExecutionContext(String extractionId,
                                                      MigrationFileType migrationFileType,
                                                      long avgRowSize) {
    Path extractionDirectory = exportFileStatusDao.resolveExtractionDirectory(extractionId);
    String extractionTimestamp =
      FILE_TIMESTAMP_FORMATTER.format(LocalDateTime.now(Constants.ZONEID));
    String brokerCf = getRequiredBrokerCf();
    String zipVersion = getZipVersion();
    int maxRowsPerPart = Math.max(
      1,
      (int) (extractorExportProperties.multipartMaxFileSize() / avgRowSize)
    );

    return new ExportExecutionContext(
      extractionDirectory,
      migrationFileType,
      brokerCf,
      zipVersion,
      extractionTimestamp,
      maxRowsPerPart,
      extractorExportProperties.exportPageSize()
    );
  }

  /**
   * Retrieves the required broker certification number from configuration.
   *
   * @return the broker certification number
   * @throws IllegalStateException if the broker certification is not configured or is blank
   */
  private String getRequiredBrokerCf() {
    String brokerCf = extractorExportProperties.brokerCf();
    if (brokerCf == null || brokerCf.isBlank()) {
      throw new IllegalStateException("Missing required brokerCf configuration for ZIP naming");
    }
    return brokerCf;
  }

  /**
   * Gets the ZIP version string for export file naming.
   *
   * @return the ZIP version string
   */
  private String getZipVersion() {
    return ExportFileVersion.V1_0.getValue();
  }

  /**
   * Retrieves the source data for the current extraction request.
   * <p>
   * Subclasses must implement this method to provide the actual data source,
   * which may be loaded from a database, API, or file.
   *
   * @param request the extraction request with filtering and configuration details
   * @return the list of source entities to export
   */
  protected abstract List<I> retrieveData(ExtractionRequest request);

  /**
   * Gets the migration file type for this export.
   * <p>
   * Subclasses must implement this to specify what type of migration file is being produced.
   *
   * @return the migration file type
   */
  protected abstract MigrationFileType getMigrationFileType();

  /**
   * Gets the DTO class used for exporting entities.
   * <p>
   * This class is used to determine CSV headers and field mapping.
   *
   * @return the output entity class
   */
  protected abstract Class<O> getDtoClass();

  /**
   * Gets the average size in bytes of a single row in the export.
   * <p>
   * This value is used to calculate how many rows can fit in a single export file
   * given the configured maximum file size.
   *
   * @return the average row size in bytes
   */
  protected abstract long getAvgRowSize();

  /**
   * Transforms an input entity into its exportable form.
   * <p>
   * Subclasses implement this to convert from the source data format (type I)
   * to the export format (type O).
   *
   * @param entity the input entity to transform
   * @return the exportable entity
   */
  protected abstract O toExportableEntity(I entity);
}
