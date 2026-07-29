package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionstypeorgoperators.DebtPositionsTypeOrgOperatorsExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontype.DebtPositionTypeExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.orgsil.OrgSilServiceExportProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataExportFacadeServiceTest {

  @Mock
  private OrganizationExportProcessingService organizationExportProcessingServiceMock;
  @Mock
  private OrgSilServiceExportProcessingService orgSilServiceExportProcessingServiceMock;
  @Mock
  private DebtPositionTypeExportProcessingService debtPositionTypeExportProcessingServiceMock;
  @Mock
  private DebtPositionsTypeOrgOperatorsExportProcessingService debtPositionsTypeOrgOperatorsExportProcessingServiceMock;

  @InjectMocks
  private DataExportFacadeService service;


  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      ignoreStubs(
        organizationExportProcessingServiceMock,
        orgSilServiceExportProcessingServiceMock,
        debtPositionTypeExportProcessingServiceMock,
        debtPositionsTypeOrgOperatorsExportProcessingServiceMock
      )
    );
  }

  @ParameterizedTest
  @EnumSource(MigrationFileType.class)
  void whenExecuteExportThenRouteByFileType(MigrationFileType fileType) {
    String extractionId = "extraction-id";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", fileType);
    ExportFileResult expected = null;

    switch (fileType) {
      case ORGANIZATIONS -> {
        expected = new ExportFileResult(List.of("organizations_1_0.zip"), null);
        when(organizationExportProcessingServiceMock.executeExport(extractionId, request)).thenReturn(expected);
      }
      case ORG_SIL_SERVICES -> {
        expected = new ExportFileResult(List.of("orgsilservices_1_0.zip"), null);
        when(orgSilServiceExportProcessingServiceMock.executeExport(extractionId, request)).thenReturn(expected);
      }
      case DEBT_POSITIONS_TYPE -> {
        expected = new ExportFileResult(List.of("debtpositionstype_1_0.zip"), null);
        when(debtPositionTypeExportProcessingServiceMock.executeExport(extractionId, request)).thenReturn(expected);
      }
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS -> {
        expected = new ExportFileResult(List.of("debtpositionstypeorgoperators_1_0.zip"), null);
        when(debtPositionsTypeOrgOperatorsExportProcessingServiceMock.executeExport(extractionId, request)).thenReturn(expected);
      }
      default -> {
        ExportFileTypeNotSupportedException exception = assertThrows(
          ExportFileTypeNotSupportedException.class,
          () -> service.executeExport(extractionId, request)
        );

        assertEquals("EXPORT_FILE_NOT_SUPPORTED", exception.getCode());
        assertEquals("Invalid export file type: " + fileType, exception.getMessage());
      }
    }

    if (expected != null) {
      ExportFileResult result = service.executeExport(extractionId, request);
      assertEquals(expected, result);
    }
  }
}
