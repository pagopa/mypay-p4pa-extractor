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

/**
 * Base class for export services that produce one file per requested IPA code and
 * receive the resolved IPA code directly in the data retrieval method.
 *
 * <p>Subclasses inherit a per-IPA export behaviour by overriding
 * {@link #executeExport(ExtractionRequest, Path, int, List, List)} to split the request by IPA
 * and run one export per code. This class further delegates row retrieval to
 * {@link #retrieveData(String, ExtractionRequest, int, int)}, sparing subclasses from extracting
 * the IPA code from the request themselves.
 *
 * @param <E> source model type retrieved from the data source
 * @param <C> CSV export DTO type
 */
public abstract class SplitByIpaCodeBaseExportProcessingService<E extends ExportModel, C extends CsvExportDto>
  extends BaseExportProcessingService<E, C> {

  protected SplitByIpaCodeBaseExportProcessingService(CsvService csvService,
                                                       CsvPartitionWriterService csvPartitionWriterService,
                                                       FileArchiverService fileArchiverService,
                                                       Validator validator,
                                                       ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
  }

  @Override
  protected void executeExport(ExtractionRequest request,
                               Path workingDirectory,
                               int pageSize,
                               List<Path> csvFilePaths,
                               List<Path> errorFilePaths) throws IOException {
    for (String ipaCode : request.getIpaCodes()) {
      ExtractionRequest singleRequest = request.toBuilder()
        .ipaCodes(List.of(ipaCode))
        .build();

      super.executeExport(
        singleRequest,
        workingDirectory,
        pageSize,
        csvFilePaths,
        errorFilePaths
      );
    }
  }

  @Override
  protected final List<E> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    String ipaCode = request.getIpaCodes().getFirst();
    return retrieveData(ipaCode, request, pageSize, offset);
  }

  @Override
  protected boolean useBrokerIpaAsPrefix() {
    return false;
  }

  /**
   * Retrieves a page of source data for a single IPA code.
   *
   * @param ipaCode  the IPA code extracted from the request
   * @param request  full extraction request (contains filters and other context)
   * @param pageSize maximum number of records to retrieve
   * @param offset   starting offset for pagination
   * @return list of retrieved records
   */
  protected abstract List<E> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset);
}
