package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.orgsil.OrgSilServiceExportProcessingService;
import org.springframework.stereotype.Service;

@Service
public class DataExportFacadeService {

  private final OrganizationExportProcessingService organizationExportProcessingService;
  private final OrgSilServiceExportProcessingService orgSilServiceExportProcessingService;

  public DataExportFacadeService(OrganizationExportProcessingService organizationExportProcessingService,
                                 OrgSilServiceExportProcessingService orgSilServiceExportProcessingService) {
    this.organizationExportProcessingService = organizationExportProcessingService;
    this.orgSilServiceExportProcessingService = orgSilServiceExportProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return switch (request.getFileTypes()) {
      case ORGANIZATIONS -> organizationExportProcessingService.executeExport(extractionId, request);
      case ORG_SIL_SERVICES -> orgSilServiceExportProcessingService.executeExport(extractionId, request);
      default ->
        throw new ExportFileTypeNotSupportedException("Invalid export file type: " + request.getFileTypes());
    };
  }
}
