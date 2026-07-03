package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileHandlerServiceTest {

  @Mock
  private DataExportFacadeService dataExportFacadeServiceMock;

  @InjectMocks
  private ExportFileHandlerService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(dataExportFacadeServiceMock);
  }

  @Test
  void givenValidRequestWhenExecuteExportThenReturnFiles() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    assertEquals(List.of("organizations.csv"), exportFileResult.files());
    assertNull(exportFileResult.error());
  }

  @Test
  void givenFacadeThrowsExceptionWhenExecuteExportThenResultContainsError() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of(), "export failed");

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    assertEquals(List.of(), exportFileResult.files());
    assertEquals("export failed", exportFileResult.error());
  }
}
