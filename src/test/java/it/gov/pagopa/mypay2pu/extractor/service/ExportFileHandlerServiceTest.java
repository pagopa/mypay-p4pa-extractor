package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    service.executeExport(extractionId, request);

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

    service.executeExport(extractionId, request);

    verify(dataExportFacadeService).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    verifyNoMoreInteractions(dataExportFacadeService);
  }
}
