package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.config.PaymentsReportingSource;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare.MypaySharePaymentsReportingExportProcessingService;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingExportProcessingServiceTest {

  @Mock
  private MypaySharePaymentsReportingExportProcessingService mypayShareServiceMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(mypayShareServiceMock);
  }

  @Test
  void givenMypayShareSourceWhenExportThenDelegateToMypayShareService() {
    ExtractionRequest request = request();
    ExportFileResult expected = new ExportFileResult(List.of("payments-reporting.zip"), null);
    when(mypayShareServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service(PaymentsReportingSource.MYPAY_SHARE).executeExport("extraction-id", request);

    assertEquals(expected, result);
    verify(mypayShareServiceMock).executeExport("extraction-id", request);
  }

  @Test
  void givenMypivotSourceWhenExportThenThrowNotImplementedException() {
    PaymentsReportingExportProcessingService service = service(PaymentsReportingSource.MYPIVOT);
    ExtractionRequest request = request();

    NotImplementedException exception = assertThrows(
      NotImplementedException.class,
      () -> service.executeExport("extraction-id", request)
    );

    assertEquals("Payments reporting source is not implemented: MYPIVOT", exception.getMessage());
  }

  private PaymentsReportingExportProcessingService service(PaymentsReportingSource source) {
    ExtractorExportProperties properties = new ExtractorExportProperties(
      "storage",
      "temp",
      "BROKER_CF",
      "BROKER_IPA",
      Map.of(MigrationFileType.PAYMENTS_REPORTING, new ExtractorExportProperties.FileTypeConfiguration(1)),
      new ExtractorExportProperties.PaymentsReportingConfiguration(source)
    );
    return new PaymentsReportingExportProcessingService(properties, mypayShareServiceMock);
  }

  private ExtractionRequest request() {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING);
  }
}
