package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationExportProcessingService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportFacadeServiceTest {

  @Mock
  private OrganizationExportProcessingService organizationExportProcessingServiceMock;

  @InjectMocks
  private DataExportFacadeService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(organizationExportProcessingServiceMock);
  }

  @Test
  void whenExecuteOrganizationsExportThenDelegateToOrganizationProcessor() {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.ORGANIZATIONS);
    ExportFileResult expected = new ExportFileResult(List.of("organizations_1_0.zip"), null);
    when(organizationExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }

  @Test
  void whenExecuteExportThenRejectUnsupportedFileType() {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.DEBT_POSITIONS);

    ExportFileTypeNotSupportedException exception = assertThrows(
      ExportFileTypeNotSupportedException.class,
      () -> service.executeExport("extraction-id", request)
    );

    assertEquals("EXPORT_FILE_NOT_SUPPORTED", exception.getCode());
    assertEquals("Invalid export file type: DEBT_POSITIONS", exception.getMessage());
  }
}
