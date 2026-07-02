package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportFileHandlerService {

  private final DataExportFacadeService dataExportFacadeService;

  public ExportFileHandlerService(DataExportFacadeService dataExportFacadeService) {
    this.dataExportFacadeService = dataExportFacadeService;
  }

  public ExtractionStatusResponse executeExport(String extractionId, ExtractionRequest request) {
    log.info("Processing extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());
    //TODO the export status logic handle will be implemented by task P4ADEV-4816
    ExportFileResult exportFileResult = dataExportFacadeService.executeExport(extractionId, request.getFileTypes());
    return new ExtractionStatusResponse()
      .extractionId(extractionId)
      .ipaCode(request.getIpaCode())
      .fileTypes(request.getFileTypes())
      .files(exportFileResult.files())
      .error(exportFileResult.error());
  }
}
