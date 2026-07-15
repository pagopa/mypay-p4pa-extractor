package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.OrganizationExportProcessingService;
import org.springframework.stereotype.Component;

@Component
public class DataExportFacadeService {

  private final OrganizationExportProcessingService organizationExportProcessingService;

  public DataExportFacadeService(OrganizationExportProcessingService organizationExportProcessingService) {
    this.organizationExportProcessingService = organizationExportProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return switch (request.getFileTypes()) {
      case ORGANIZATIONS -> organizationExportProcessingService.executeExport(extractionId, request);
      default ->
        throw new ExportFileTypeNotSupportedException("Invalid export file type: " + request.getFileTypes());
    };
  }
}
