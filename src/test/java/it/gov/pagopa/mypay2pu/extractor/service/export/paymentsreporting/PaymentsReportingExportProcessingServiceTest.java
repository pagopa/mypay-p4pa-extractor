package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare.MypaySharePaymentsReportingExportProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  void whenExportThenDelegateToMypayShareService() {
    ExtractionRequest request = request();
    ExportFileResult expected = new ExportFileResult(
      List.of(new ArchiveFile("payments-reporting.zip", List.of())),
      null
    );
    when(mypayShareServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(expected, result);
    verify(mypayShareServiceMock).executeExport("extraction-id", request);
  }

  private PaymentsReportingExportProcessingService service() {
    return new PaymentsReportingExportProcessingService(mypayShareServiceMock);
  }

  private ExtractionRequest request() {
    return new ExtractionRequest(List.of("IPA_CODE"), it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.PAYMENTS_REPORTING);
  }
}
