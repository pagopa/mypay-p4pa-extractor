package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportFileHandlerService {

  private final DataExportFacadeService dataExportFacadeService;

  public ExportFileHandlerService(DataExportFacadeService dataExportFacadeService) {
    this.dataExportFacadeService = dataExportFacadeService;
  }

  @Async("extractorTaskExecutor")
  public void executeExport(String extractionId, ExtractionRequest request) {
    log.info("Processing extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());
    //TODO the export status logic handle will be implemented by task P4ADEV-4816
    dataExportFacadeService.executeExport(extractionId, request.getFileTypes());
  }
}
