package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * CSV specialization of the common export lifecycle.
 *
 * <p>It retrieves source rows in pages, maps and validates them, writes CSV
 * partitions, and creates an optional CSV error report.</p>
 *
 * @param <E> source model type
 * @param <C> CSV DTO type
 */
public abstract class BaseCsvExportProcessingService<E extends ExportModel, C extends CsvExportDto>
  extends BaseExportProcessingService<C> {

  private final CsvService csvService;
  private final Validator validator;

  protected BaseCsvExportProcessingService(CsvService csvService,
                                           CsvPartitionWriterService<C> csvPartitionWriterService,
                                           FileArchiverService fileArchiverService,
                                           Validator validator,
                                           ExtractorExportProperties exportProperties) {
    super(csvPartitionWriterService, fileArchiverService, exportProperties);
    this.csvService = csvService;
    this.validator = validator;
  }

  @Override
  protected ExportGenerationResult generateExport(ExtractionRequest request,
                                                  Path workingDirectory,
                                                  int pageSize,
                                                  ExportFileNameBuilder fileNameBuilder) throws IOException {
    Path baseCsvFilePath = workingDirectory.resolve(fileNameBuilder.buildCsvFileName());
    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, baseCsvFilePath)) {
      Supplier<List<C>> rowsSupplier = new CsvValidatedRowSupplier<>(
        new PaginatedExportRowsSupplier<>((limit, offset) -> retrieveData(request, limit, offset), pageSize),
        this::toExportableEntity,
        validator,
        errorCollector
      );
      List<Path> csvFiles = partitionWriter().writePartitions(
        workingDirectory, fileNameBuilder, getDtoClass(), rowsSupplier, getZipVersion(), pageSize
      );
      Optional<Path> errorFile = errorCollector.writeToFile(baseCsvFilePath);
      return new ExportGenerationResult(csvFiles, errorFile.map(List::of).orElseGet(List::of));
    }
  }

  /** @return DTO class used by the CSV serializer */
  protected abstract Class<C> getDtoClass();

  /**
   * Maps a source model to its CSV representation.
   *
   * @param model source model
   * @return mapped CSV DTO
   */
  protected abstract C toExportableEntity(E model);

  /**
   * Retrieves one source page.
   *
   * @param request extraction request
   * @param pageSize maximum records to retrieve
   * @param offset page offset
   * @return source records
   */
  protected abstract List<E> retrieveData(ExtractionRequest request, int pageSize, int offset);
}
