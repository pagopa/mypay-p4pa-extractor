package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import java.nio.file.Path;
import java.util.List;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
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
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    doAnswer(invocation -> {
      assertNotNull(invocation.<ExtractionRequest>getArgument(0).getFilters());
      return null;
    })
      .when(extractionValidationFacadeMock)
      .validate(any(ExtractionRequest.class));

    String extractionId = service.createExtraction(request);

    assertNotNull(extractionId);
    assertFalse(extractionId.isBlank());
    InOrder validationOrder = inOrder(extractionValidationFacadeMock, exportFileStatusServiceMock, extractorTaskExecutorMock);
    validationOrder.verify(extractionValidationFacadeMock).validate(request);
    validationOrder.verify(exportFileStatusServiceMock).createNew(extractionId, request);
    validationOrder.verify(extractorTaskExecutorMock).execute(runnableCaptor.capture());
    assertNotNull(request.getFilters());
  }

  @Test
  void givenAcceptedExtractionWhenGetStatusBeforeTaskStartsThenReadNewStatus(@TempDir Path tempDir) {
    ExportFileStatusService statusService = new ExportFileStatusService(
      new ExportFileStatusDao(new JsonConfig().objectMapperJackson3(), tempDir.toString())
    );
    ExportFileHandlerService handler = new ExportFileHandlerService(
      dataExportFacadeServiceMock, statusService, extractorTaskExecutorMock,
      extractionValidationFacadeMock, extractionRequestValidatorMock
    );
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    String extractionId = handler.createExtraction(request);
    ExtractionStatusResponse initialStatus = handler.getExtractionStatus(extractionId);

    assertEquals(ExtractionStatus.NEW, initialStatus.getStatus());
    assertEquals(extractionId, initialStatus.getExtractionId());
    assertEquals(request.getIpaCodes(), initialStatus.getIpaCodes());
    verify(extractionValidationFacadeMock).validate(request);
    verify(extractionRequestValidatorMock).validateExtractionId(extractionId);
    verify(extractorTaskExecutorMock).execute(runnableCaptor.capture());

    when(dataExportFacadeServiceMock.executeExport(extractionId, request)).thenAnswer(invocation -> {
      assertEquals(ExtractionStatus.RUNNING, statusService.readStatus(extractionId).getStatus());
      assertEquals(initialStatus.getCreatedAt(), statusService.readStatus(extractionId).getCreatedAt());
      return new ExportFileResult(List.of(), null);
    });

    runnableCaptor.getValue().run();

    assertEquals(ExtractionStatus.COMPLETED, statusService.readStatus(extractionId).getStatus());
    verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
  }

  @Test
  void givenValidRequestWhenExecuteExportThenStoreCompletedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(
      List.of(new ArchiveFile("organizations.csv", List.of())),
      null
    );

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, ExtractionStatus.RUNNING);
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
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, ExtractionStatus.RUNNING);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    verify(exportFileStatusServiceMock).update(
      extractionId,
      new ExportFileResult(List.<ArchiveFile>of(), "export failed")
    );
  }

  @Test
  void givenFacadeReturnsErrorWhenExecuteExportThenStoreFailedStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult exportFileResult = new ExportFileResult(
      List.of(new ArchiveFile("organizations.csv", List.of())),
      "result error"
    );

    when(dataExportFacadeServiceMock.executeExport(extractionId, request))
      .thenReturn(exportFileResult);

    service.executeExport(extractionId, request);

    InOrder inOrder = inOrder(exportFileStatusServiceMock, dataExportFacadeServiceMock);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, ExtractionStatus.RUNNING);
    inOrder.verify(dataExportFacadeServiceMock).executeExport(extractionId, request);
    inOrder.verify(exportFileStatusServiceMock).update(extractionId, exportFileResult);
  }

  @Test
  void givenInvalidRequestWhenCreateExtractionThenDoNotScheduleExport() {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE_TEST"),
      MigrationFileType.DEBT_POSITIONS,
      null,
      new ExtractionFilters()
    );
    doThrow(new BadRequestException("INVALID_EXTRACTION_FILTERS", "invalid logical key"))
      .when(extractionValidationFacadeMock)
      .validate(request);

    assertThrows(BadRequestException.class, () -> service.createExtraction(request));

    verify(extractionValidationFacadeMock).validate(request);
  }

  @Test
  void givenStatusCreationFailsWhenCreateExtractionThenDoNotScheduleExport() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    RuntimeException failure = new RuntimeException("status write failed");
    doThrow(failure).when(exportFileStatusServiceMock).createNew(anyString(), any(ExtractionRequest.class));

    assertSame(failure, assertThrows(RuntimeException.class, () -> service.createExtraction(request)));

    verify(extractionValidationFacadeMock).validate(request);
    verify(exportFileStatusServiceMock).createNew(anyString(), any(ExtractionRequest.class));
  }

  @Test
  void givenExecutorRejectsTaskWhenCreateExtractionThenStoreFailedResultAndPropagateRejection() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    TaskRejectedException rejection = new TaskRejectedException("executor full");
    doThrow(rejection).when(extractorTaskExecutorMock).execute(any(Runnable.class));

    assertSame(rejection, assertThrows(TaskRejectedException.class, () -> service.createExtraction(request)));

    ArgumentCaptor<String> extractionIdCaptor = ArgumentCaptor.forClass(String.class);
    InOrder order = inOrder(extractionValidationFacadeMock, exportFileStatusServiceMock, extractorTaskExecutorMock);
    order.verify(extractionValidationFacadeMock).validate(request);
    order.verify(exportFileStatusServiceMock).createNew(extractionIdCaptor.capture(), any(ExtractionRequest.class));
    order.verify(extractorTaskExecutorMock).execute(any(Runnable.class));
    order.verify(exportFileStatusServiceMock).update(
      extractionIdCaptor.getValue(), new ExportFileResult(List.of(), "Extraction task rejected")
    );
  }

  @Test
  void givenRejectedTaskAndStatusUpdateFailsWhenCreateExtractionThenPreserveOriginalRejection() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    TaskRejectedException rejection = new TaskRejectedException("executor full");
    RuntimeException failure = new RuntimeException("status update failed");
    doThrow(rejection).when(extractorTaskExecutorMock).execute(any(Runnable.class));
    doThrow(failure).when(exportFileStatusServiceMock).update(anyString(), any(ExportFileResult.class));

    TaskRejectedException thrown = assertThrows(TaskRejectedException.class, () -> service.createExtraction(request));

    assertSame(rejection, thrown);
    assertEquals(1, thrown.getSuppressed().length);
    assertSame(failure, thrown.getSuppressed()[0]);
    verify(extractionValidationFacadeMock).validate(request);
    verify(exportFileStatusServiceMock).createNew(anyString(), any(ExtractionRequest.class));
    verify(extractorTaskExecutorMock).execute(any(Runnable.class));
    verify(exportFileStatusServiceMock).update(anyString(), any(ExportFileResult.class));
  }

  @Test
  void givenRunningStatusUpdateFailsWhenExecuteExportThenDoNotStartExport() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE_TEST"), MigrationFileType.ORGANIZATIONS);
    RuntimeException failure = new RuntimeException("status update failed");
    doThrow(failure).when(exportFileStatusServiceMock).update(extractionId, ExtractionStatus.RUNNING);

    assertSame(failure, assertThrows(RuntimeException.class, () -> service.executeExport(extractionId, request)));

    verify(exportFileStatusServiceMock).update(extractionId, ExtractionStatus.RUNNING);
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
      List.of(new ArchiveFile("archive1.zip", List.of("organizations.csv")))
    );
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(statusResponse);

    ExtractionStatusResponse result = service.getExtractionStatus(extractionId);

    assertEquals(statusResponse, result);
    InOrder inOrder = inOrder(extractionRequestValidatorMock, exportFileStatusServiceMock);
    inOrder.verify(extractionRequestValidatorMock).validateExtractionId(extractionId);
    inOrder.verify(exportFileStatusServiceMock).readStatus(extractionId);
  }
}
