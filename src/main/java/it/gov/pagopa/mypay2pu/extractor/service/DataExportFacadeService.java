package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import it.gov.pagopa.mypay2pu.extractor.service.export.organization.OrganizationProcessingService;
import org.springframework.stereotype.Component;

@Component
public class DataExportFacadeService {

  private final OrganizationProcessingService organizationProcessingService;

  public DataExportFacadeService(OrganizationProcessingService organizationProcessingService) {
    this.organizationProcessingService = organizationProcessingService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return switch (request.getFileTypes()) {
      case ORGANIZATIONS -> organizationProcessingService.extract(extractionId, request);
      default ->
        throw new ExportFileTypeNotSupportedException("Invalid export file type: " + request.getFileTypes());
    };
  }
}
