package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileHandlerServiceTest {

  @Mock
  private DataExportFacadeService dataExportFacadeServiceMock;
  @Mock
  private ExportFileStatusService exportFileStatusServiceMock;

  @InjectMocks
  private ExportFileHandlerService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(dataExportFacadeServiceMock, exportFileStatusServiceMock);
  }

  @Test
  void givenValidRequestWhenExecuteExportThenStoreCompletedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);
    ExtractionStatusResponse runningStatus = new ExtractionStatusResponse(
      extractionId,
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      null,
      List.of()
    );

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(runningStatus);

    service.executeExport(extractionId, request);

    ArgumentCaptor<ExtractionStatusResponse> statusCaptor = ArgumentCaptor.forClass(ExtractionStatusResponse.class);
    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).readStatus(extractionId);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());

    List<ExtractionStatusResponse> writtenStatuses = statusCaptor.getAllValues();
    ExtractionStatusResponse initialStatus = writtenStatuses.get(0);
    ExtractionStatusResponse finalStatus = writtenStatuses.get(1);

    assertEquals(ExtractionStatus.RUNNING, initialStatus.getStatus());
    assertEquals(extractionId, initialStatus.getExtractionId());
    assertEquals("IPA_CODE_TEST", initialStatus.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, initialStatus.getFileTypes());
    assertEquals(List.of(), initialStatus.getFiles());
    assertNotNull(initialStatus.getCreatedAt());
    assertNotNull(initialStatus.getUpdatedAt());

    assertEquals(ExtractionStatus.COMPLETED, finalStatus.getStatus());
    assertEquals(List.of("organizations.csv"), finalStatus.getFiles());
    assertEquals(extractionId, finalStatus.getExtractionId());
    assertEquals("IPA_CODE_TEST", finalStatus.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, finalStatus.getFileTypes());
    assertNull(finalStatus.getError());
    assertNotNull(finalStatus.getUpdatedAt());
  }

  @Test
  void givenFacadeThrowsExceptionWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExtractionStatusResponse runningStatus = new ExtractionStatusResponse(
      extractionId,
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      null,
      List.of()
    );

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenThrow(new RuntimeException("export failed"));
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(runningStatus);

    service.executeExport(extractionId, request);

    ArgumentCaptor<ExtractionStatusResponse> statusCaptor = ArgumentCaptor.forClass(ExtractionStatusResponse.class);
    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).readStatus(extractionId);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());

    ExtractionStatusResponse finalStatus = statusCaptor.getAllValues().get(1);
    assertEquals(ExtractionStatus.FAILED, finalStatus.getStatus());
    assertEquals("export failed", finalStatus.getError());
    assertEquals(List.of(), finalStatus.getFiles());
  }

  @Test
  void givenFacadeReturnsErrorWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), "result error");
    ExtractionStatusResponse runningStatus = new ExtractionStatusResponse(
      extractionId,
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      null,
      List.of()
    );

    when(dataExportFacadeServiceMock.executeExport(extractionId, MigrationFileType.ORGANIZATIONS))
      .thenReturn(exportFileResult);
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(runningStatus);

    service.executeExport(extractionId, request);

    ArgumentCaptor<ExtractionStatusResponse> statusCaptor = ArgumentCaptor.forClass(ExtractionStatusResponse.class);
    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, MigrationFileType.ORGANIZATIONS);
    inOrder.verify(exportFileStatusServiceMock).readStatus(extractionId);
    inOrder.verify(exportFileStatusServiceMock).writeStatus(statusCaptor.capture());

    ExtractionStatusResponse finalStatus = statusCaptor.getAllValues().get(1);
    assertEquals(ExtractionStatus.FAILED, finalStatus.getStatus());
    assertEquals("result error", finalStatus.getError());
    assertEquals(List.of("organizations.csv"), finalStatus.getFiles());
  }
}
