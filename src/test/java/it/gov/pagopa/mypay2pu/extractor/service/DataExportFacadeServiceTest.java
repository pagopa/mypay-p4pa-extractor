package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtposition.DebtPositionExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontype.DebtPositionTypeExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontypeorg.DebtPositionTypeOrgExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.orgsil.OrgSilServiceExportProcessingService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportFacadeServiceTest {

  @Mock
  private OrganizationExportProcessingService organizationExportProcessingServiceMock;
  @Mock
  private OrgSilServiceExportProcessingService orgSilServiceExportProcessingServiceMock;
  @Mock
  private DebtPositionTypeExportProcessingService debtPositionTypeExportProcessingServiceMock;
  @Mock
  private DebtPositionTypeOrgExportProcessingService debtPositionTypeOrgExportProcessingServiceMock;
  @Mock
  private DebtPositionExportProcessingService debtPositionExportProcessingServiceMock;

  @InjectMocks
  private DataExportFacadeService service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      organizationExportProcessingServiceMock,
      orgSilServiceExportProcessingServiceMock,
      debtPositionTypeExportProcessingServiceMock,
      debtPositionTypeOrgExportProcessingServiceMock,
      debtPositionExportProcessingServiceMock
    );
  }

  @Test
  void whenExecuteOrganizationsExportThenDelegateToOrganizationProcessor() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.ORGANIZATIONS);
    ExportFileResult expected = new ExportFileResult(List.of("organizations_1_0.zip"), null);
    when(organizationExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }

  @Test
  void whenExecuteOrgSilServicesExportThenDelegateToOrgSilServiceProcessor() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.ORG_SIL_SERVICES);
    ExportFileResult expected = new ExportFileResult(List.of("orgsilservices_1_0.zip"), null);
    when(orgSilServiceExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }

  @Test
  void whenExecuteDebtPositionsTypeExportThenDelegateToDebtPositionTypeProcessor() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.DEBT_POSITIONS_TYPE);
    ExportFileResult expected = new ExportFileResult(List.of("debtpositionstype_1_0.zip"), null);
    when(debtPositionTypeExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }

  @Test
  void whenExecuteDebtPositionsTypeOrgExportThenDelegateToDebtPositionTypeOrgProcessor() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.DEBT_POSITIONS_TYPE_ORG);
    ExportFileResult expected = new ExportFileResult(List.of("debtpositionstypeorg_1_0.zip"), null);
    when(debtPositionTypeOrgExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }

  @Test
  void whenExecuteDebtPositionsExportThenDelegateToDebtPositionProcessor() {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.DEBT_POSITIONS);
    ExportFileResult expected = new ExportFileResult(List.of("debtpositions_2_0.zip"), null);
    when(debtPositionExportProcessingServiceMock.executeExport("extraction-id", request)).thenReturn(expected);

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertEquals(expected, result);
  }
}
