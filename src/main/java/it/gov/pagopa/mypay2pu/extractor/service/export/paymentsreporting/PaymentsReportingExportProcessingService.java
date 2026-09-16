package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare.MypaySharePaymentsReportingExportProcessingService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service
public class PaymentsReportingExportProcessingService {

  private final ExtractorExportProperties extractorExportProperties;
  private final MypaySharePaymentsReportingExportProcessingService mypaySharePaymentsReportingExportProcessingService;

  public PaymentsReportingExportProcessingService(
    ExtractorExportProperties extractorExportProperties,
    MypaySharePaymentsReportingExportProcessingService mypaySharePaymentsReportingExportProcessingService
  ) {
    this.extractorExportProperties = extractorExportProperties;
    this.mypaySharePaymentsReportingExportProcessingService = mypaySharePaymentsReportingExportProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return switch (extractorExportProperties.paymentsReporting().source()) {
      case MYPAY_SHARE -> mypaySharePaymentsReportingExportProcessingService.executeExport(extractionId, request);
      case MYPIVOT -> throw new NotImplementedException(
        "Payments reporting source is not implemented: " + extractorExportProperties.paymentsReporting().source()
      );
    };
  }
}
