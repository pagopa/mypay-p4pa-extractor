package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileHandlerServiceTest {

  @Mock
  private DataExportFacadeService dataExportFacadeService;

  @InjectMocks
  private ExportFileHandlerService service;

  @Test
  void executeExportShouldDelegateAndPopulateResponse() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);

    when(dataExportFacadeService.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    ExtractionStatusResponse response = service.executeExport(extractionId, request);

    assertEquals(extractionId, response.getExtractionId());
    assertEquals("IPA_CODE_TEST", response.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, response.getFileTypes());
    assertEquals(List.of("organizations.csv"), response.getFiles());
    assertNull(response.getError());
    verify(dataExportFacadeService).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    verifyNoMoreInteractions(dataExportFacadeService);
  }

  @Test
  void executeExportShouldPropagateFacadeError() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of(), "export failed");

    when(dataExportFacadeService.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    ExtractionStatusResponse response = service.executeExport(extractionId, request);

    assertEquals("export failed", response.getError());
    assertEquals(List.of(), response.getFiles());
    verify(dataExportFacadeService).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    verifyNoMoreInteractions(dataExportFacadeService);
  }
}
