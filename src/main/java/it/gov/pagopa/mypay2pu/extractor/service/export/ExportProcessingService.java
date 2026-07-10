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

public abstract class ExportProcessingService<I, O> {
  private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final ExtractorExportProperties extractorExportProperties;
  private final ExportBatchCoordinator exportBatchCoordinator;
  private final ExportFilePartWriter exportFilePartWriter;
  private final ExportFileStatusDao exportFileStatusDao;

  protected ExportProcessingService(ExtractorExportProperties extractorExportProperties,
                                    ExportBatchCoordinator exportBatchCoordinator,
                                    ExportFilePartWriter exportFilePartWriter,
                                    ExportFileStatusDao exportFileStatusDao) {
    this.extractorExportProperties = extractorExportProperties;
    this.exportBatchCoordinator = exportBatchCoordinator;
    this.exportFilePartWriter = exportFilePartWriter;
    this.exportFileStatusDao = exportFileStatusDao;
  }

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

  protected final Supplier<List<I>> createSingleBatchSupplier(List<I> data) {
    return exportBatchCoordinator.createSingleBatchSupplier(data);
  }

  protected final Supplier<List<I>> createPagedSupplier(
    int pageSize,
    IntFunction<List<I>> pageRetriever
  ) {
    return exportBatchCoordinator.createPagedSupplier(pageSize, pageRetriever);
  }

  protected Supplier<List<I>> retrieveDataSupplier(ExtractionRequest request,
                                                   ExportExecutionContext executionContext) {
    return createSingleBatchSupplier(retrieveData(request));
  }

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

  private void collectPartResult(ExportPartResult partResult,
                                 List<String> generatedFiles,
                                 List<String> generatedErrorFiles) {
    generatedFiles.add(partResult.fileName());
    partResult.errorFileName().ifPresent(generatedErrorFiles::add);
  }

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

  private String getRequiredBrokerCf() {
    String brokerCf = extractorExportProperties.brokerCf();
    if (brokerCf == null || brokerCf.isBlank()) {
      throw new IllegalStateException("Missing required brokerCf configuration for ZIP naming");
    }
    return brokerCf;
  }

  private String getZipVersion() {
    return ExportFileVersion.V1_0.getValue();
  }

  protected abstract List<I> retrieveData(ExtractionRequest request);

  protected abstract MigrationFileType getMigrationFileType();

  protected abstract Class<O> getDtoClass();

  protected abstract long getAvgRowSize();

  protected abstract O toExportableEntity(I entity);
}
