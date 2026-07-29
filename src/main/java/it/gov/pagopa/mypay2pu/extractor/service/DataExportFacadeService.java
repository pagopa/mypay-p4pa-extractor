package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionstypeorgoperators.DebtPositionsTypeOrgOperatorsExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontype.DebtPositionTypeExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontypeorg.DebtPositionTypeOrgExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.orgsil.OrgSilServiceExportProcessingService;
import org.springframework.stereotype.Service;

@Service
public class DataExportFacadeService {

  private final OrganizationExportProcessingService organizationExportProcessingService;
  private final OrgSilServiceExportProcessingService orgSilServiceExportProcessingService;
  private final DebtPositionTypeExportProcessingService debtPositionTypeExportProcessingService;
  private final DebtPositionTypeOrgExportProcessingService debtPositionTypeOrgExportProcessingService;
  private final DebtPositionsTypeOrgOperatorsExportProcessingService debtPositionsTypeOrgOperatorsExportProcessingService;

  public DataExportFacadeService(OrganizationExportProcessingService organizationExportProcessingService,
                                 OrgSilServiceExportProcessingService orgSilServiceExportProcessingService,
                                 DebtPositionTypeExportProcessingService debtPositionTypeExportProcessingService,
                                 DebtPositionTypeOrgExportProcessingService debtPositionTypeOrgExportProcessingService,
                                 DebtPositionsTypeOrgOperatorsExportProcessingService debtPositionsTypeOrgOperatorsExportProcessingService) {
    this.organizationExportProcessingService = organizationExportProcessingService;
    this.orgSilServiceExportProcessingService = orgSilServiceExportProcessingService;
    this.debtPositionTypeExportProcessingService = debtPositionTypeExportProcessingService;
    this.debtPositionTypeOrgExportProcessingService = debtPositionTypeOrgExportProcessingService;
    this.debtPositionsTypeOrgOperatorsExportProcessingService = debtPositionsTypeOrgOperatorsExportProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return switch (request.getFileTypes()) {
      case ORGANIZATIONS -> organizationExportProcessingService.executeExport(extractionId, request);
      case ORG_SIL_SERVICES -> orgSilServiceExportProcessingService.executeExport(extractionId, request);
      case DEBT_POSITIONS_TYPE -> debtPositionTypeExportProcessingService.executeExport(extractionId, request);
      case DEBT_POSITIONS_TYPE_ORG -> debtPositionTypeOrgExportProcessingService.executeExport(extractionId, request);
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS -> debtPositionsTypeOrgOperatorsExportProcessingService.executeExport(extractionId, request);
      default ->
        throw new ExportFileTypeNotSupportedException("Invalid export file type: " + request.getFileTypes());
    };
  }
}
