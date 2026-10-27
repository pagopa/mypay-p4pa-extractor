package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare.MypaySharePaymentsReportingExportProcessingService;
import org.springframework.stereotype.Service;

@Service
public class PaymentsReportingExportProcessingService {

  private final MypaySharePaymentsReportingExportProcessingService mypaySharePaymentsReportingExportProcessingService;

  public PaymentsReportingExportProcessingService(
    MypaySharePaymentsReportingExportProcessingService mypaySharePaymentsReportingExportProcessingService
  ) {
    this.mypaySharePaymentsReportingExportProcessingService = mypaySharePaymentsReportingExportProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return mypaySharePaymentsReportingExportProcessingService.executeExport(extractionId, request);
  }
}
