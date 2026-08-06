package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionRequestValidator;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionValidationFacade;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
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
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
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
  @Mock
  private ExtractionValidationFacade extractionValidationFacadeMock;
  @Mock
  private ExtractionRequestValidator extractionRequestValidatorMock;

  @InjectMocks
  private ExportFileHandlerService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      dataExportFacadeServiceMock,
      exportFileStatusServiceMock,
      extractorTaskExecutorMock,
      extractionValidationFacadeMock,
      extractionRequestValidatorMock
    );
  }

  @Test
  void givenValidRequestWhenCreateExtractionThenReturnGeneratedIdAndScheduleExecution() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    String extractionId = service.createExtraction(request);

    assertNotNull(extractionId);
    assertFalse(extractionId.isBlank());
    InOrder validationOrder = inOrder(extractionValidationFacadeMock, extractorTaskExecutorMock);
    validationOrder.verify(extractionValidationFacadeMock).validate(request);
    validationOrder.verify(extractorTaskExecutorMock).execute(runnableCaptor.capture());

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenReturn(exportFileResult);

    runnableCaptor.getValue().run();

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    ArgumentCaptor<String> extractionIdCaptor = ArgumentCaptor.forClass(String.class);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionIdCaptor.capture(), eq(request));
    assertEquals(extractionId, extractionIdCaptor.getValue());
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenValidRequestWhenExecuteExportThenStoreCompletedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), null);

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenFacadeThrowsExceptionWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenThrow(new RuntimeException("export failed"));

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    verify(exportFileStatusServiceMock).update(extractionId, new ExportFileResult(List.of(), "export failed"));
  }

  @Test
  void givenFacadeReturnsErrorWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(List.of("organizations.csv"), "result error");

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenInvalidRequestWhenCreateExtractionThenDoNotScheduleExport() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.DEBT_POSITIONS);
    doThrow(new BadRequestException("INVALID_EXTRACTION_FILTERS", "invalid logical key"))
      .when(extractionValidationFacadeMock)
      .validate(request);

    assertThrows(BadRequestException.class, () -> service.createExtraction(request));

    verify(extractionValidationFacadeMock).validate(request);
  }

  @Test
  void givenValidExtractionIdWhenGetExtractionStatusThenValidateAndReturnStatus() {
    String extractionId = "123e4567-e89b-42d3-a456-426614174000";
    ExtractionStatusResponse statusResponse = new ExtractionStatusResponse(
      extractionId,
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.COMPLETED,
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      null,
      List.of("organizations.csv")
    );
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(statusResponse);

    ExtractionStatusResponse result = service.getExtractionStatus(extractionId);

    assertEquals(statusResponse, result);
    InOrder inOrder = inOrder(extractionRequestValidatorMock, exportFileStatusServiceMock);
    inOrder.verify(extractionRequestValidatorMock).validateExtractionId(extractionId);
    inOrder.verify(exportFileStatusServiceMock).readStatus(extractionId);
  }
}
