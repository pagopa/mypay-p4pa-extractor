package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileHandlerServiceTest {

  @Mock
  private DataExportFacadeService dataExportFacadeServiceMock;
  @Mock
  private ExportFileStatusService exportFileStatusServiceMock;
  @Mock
  private TaskExecutor extractorTaskExecutorMock;

  @InjectMocks
  private ExportFileHandlerService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(dataExportFacadeServiceMock, exportFileStatusServiceMock, extractorTaskExecutorMock);
  }

  @Test
  void givenValidRequestWhenCreateExtractionThenReturnGeneratedIdAndScheduleExecution() {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    String extractionId = service.createExtraction(request);

    assertNotNull(extractionId);
    assertFalse(extractionId.isBlank());
    verify(extractorTaskExecutorMock).execute(runnableCaptor.capture());

    when(dataExportFacadeServiceMock.executeExport(anyString(), eq(MigrationFileType.ORGANIZATIONS)))
      .thenReturn(exportFileResult);

    runnableCaptor.getValue().run();

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    ArgumentCaptor<String> extractionIdCaptor = ArgumentCaptor.forClass(String.class);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionIdCaptor.capture(), eq(request));
    assertEquals(extractionId, extractionIdCaptor.getValue());
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenValidRequestWhenExecuteExportThenStoreCompletedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenFacadeThrowsExceptionWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenThrow(new RuntimeException("export failed"));

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    verify(exportFileStatusServiceMock).update(extractionId, new ExportFileResult(List.of(), "export failed"));
  }

  @Test
  void givenFacadeReturnsErrorWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), "result error");

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }
}
