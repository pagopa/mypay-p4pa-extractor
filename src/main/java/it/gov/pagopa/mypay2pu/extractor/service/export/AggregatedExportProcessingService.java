package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;

/**
 * Base class for export services that produce a single file for all requested IPA codes.
 *
 * <p>Subclasses inherit an aggregated export behaviour: {@link #isExportSplitByIpaCode()}
 * always returns {@code false}, so the export is executed once for the full IPA list.
 *
 * @param <E> source model type retrieved from the data source
 * @param <C> CSV export DTO type
 */
public abstract class AggregatedExportProcessingService<E extends ExportModel, C extends CsvExportDto>
  extends BaseExportProcessingService<E, C> {

  protected AggregatedExportProcessingService(CsvService csvService,
                                               CsvPartitionWriterService csvPartitionWriterService,
                                               FileArchiverService fileArchiverService,
                                               Validator validator,
                                               ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
  }

  @Override
  protected final boolean isExportSplitByIpaCode() {
    return false;
  }
}
