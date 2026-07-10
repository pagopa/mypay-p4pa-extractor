package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataExportFacadeServiceTest {

  @Mock
  private OrganizationProcessingService organizationProcessingServiceMock;

  @InjectMocks
  private DataExportFacadeService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(organizationProcessingServiceMock);
  }

  @Test
  void givenOrganizationsRequestWhenExecuteExportThenDelegateToOrganizationProcessingService() {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );
    ExportFileResult expectedResult = new ExportFileResult(
      List.of("organizations.zip"),
      List.of("organizations-errors.csv"),
      null
    );

    when(organizationProcessingServiceMock.extract(extractionId, request)).thenReturn(expectedResult);

    ExportFileResult result = service.executeExport(extractionId, request);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenUnsupportedFileTypeWhenExecuteExportThenRejectRequest() {
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.DEBT_POSITIONS,
      new ExtractionFilters()
    );

    ExportFileTypeNotSupportedException exception = assertThrows(
      ExportFileTypeNotSupportedException.class,
      () -> service.executeExport("extraction-id", request)
    );

    assertEquals("EXPORT_FILE_NOT_SUPPORTED", exception.getCode());
    assertEquals("Invalid export file type: DEBT_POSITIONS", exception.getMessage());
  }
}
