package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileStatusServiceTest {

  @Mock
  private ExportFileStatusDao exportFileStatusDaoMock;

  @InjectMocks
  private ExportFileStatusService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(exportFileStatusDaoMock);
  }

  @Test
  void givenRequestWhenCreateNewThenStoreRunningStatus() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );

    service.createNew(extractionId, request);

    ArgumentCaptor<ExtractionStatusResponse> statusCaptor = ArgumentCaptor.forClass(ExtractionStatusResponse.class);
    verify(exportFileStatusDaoMock).writeStatus(statusCaptor.capture());

    ExtractionStatusResponse writtenStatus = statusCaptor.getValue();
    assertEquals(extractionId, writtenStatus.getExtractionId());
    assertEquals("IPA_CODE_TEST", writtenStatus.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, writtenStatus.getFileTypes());
    assertEquals(ExtractionStatus.RUNNING, writtenStatus.getStatus());
    assertEquals(List.of(), writtenStatus.getFiles());
    assertNull(writtenStatus.getError());
    assertNotNull(writtenStatus.getCreatedAt());
    assertEquals(writtenStatus.getCreatedAt(), writtenStatus.getUpdatedAt());
  }

  @ParameterizedTest
  @MethodSource("updateStatusCases")
  void givenExportResultWhenUpdateThenStoreExpectedStatus(
    ExportFileResult exportFileResult,
    ExtractionStatus expectedStatus,
    String expectedError,
    List<String> expectedFiles
  ) {
    String extractionId = "extraction-id";
    OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime previousUpdatedAt = OffsetDateTime.parse("2026-01-02T00:00:00Z");
    ExtractionStatusResponse currentStatus = new ExtractionStatusResponse(
      extractionId,
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      createdAt,
      previousUpdatedAt,
      null,
      List.of()
    );
    when(exportFileStatusDaoMock.readStatus(extractionId)).thenReturn(currentStatus);

    service.update(extractionId, exportFileResult);

    ArgumentCaptor<ExtractionStatusResponse> statusCaptor = ArgumentCaptor.forClass(ExtractionStatusResponse.class);
    verify(exportFileStatusDaoMock).readStatus(extractionId);
    verify(exportFileStatusDaoMock).writeStatus(statusCaptor.capture());

    ExtractionStatusResponse writtenStatus = statusCaptor.getValue();
    assertEquals(extractionId, writtenStatus.getExtractionId());
    assertEquals("IPA_CODE_TEST", writtenStatus.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, writtenStatus.getFileTypes());
    assertEquals(createdAt, writtenStatus.getCreatedAt());
    assertEquals(expectedStatus, writtenStatus.getStatus());
    assertEquals(expectedError, writtenStatus.getError());
    assertEquals(expectedFiles, writtenStatus.getFiles());
    assertNotNull(writtenStatus.getUpdatedAt());
    assertEquals(currentStatus, writtenStatus);
  }

  @Test
  void givenExtractionIdWhenReadStatusThenDelegateToDao() {
    String extractionId = "extraction-id";
    ExtractionStatusResponse expectedStatus = new ExtractionStatusResponse(
      extractionId,
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.COMPLETED,
      OffsetDateTime.parse("2026-01-01T00:00:00Z"),
      OffsetDateTime.parse("2026-01-02T00:00:00Z"),
      null,
      List.of("organizations.csv")
    );
    when(exportFileStatusDaoMock.readStatus(extractionId)).thenReturn(expectedStatus);

    ExtractionStatusResponse result = service.readStatus(extractionId);

    assertEquals(expectedStatus, result);
  }

  private static Stream<Arguments> updateStatusCases() {
    return Stream.of(
      Arguments.of(
        new ExportFileResult(List.of("organizations.csv"), List.of(), null),
        ExtractionStatus.COMPLETED,
        null,
        List.of("organizations.csv")
      ),
      Arguments.of(
        new ExportFileResult(List.of("organizations.csv"), List.of(), ""),
        ExtractionStatus.COMPLETED,
        null,
        List.of("organizations.csv")
      ),
      Arguments.of(
        new ExportFileResult(null, null, "   "),
        ExtractionStatus.COMPLETED,
        null,
        List.of()
      ),
      Arguments.of(
        new ExportFileResult(List.of("organizations.csv"), List.of(), "result error"),
        ExtractionStatus.FAILED,
        "result error",
        List.of("organizations.csv")
      ),
      Arguments.of(
        new ExportFileResult(null, null, "result error"),
        ExtractionStatus.FAILED,
        "result error",
        List.of()
      )
    );
  }
}
