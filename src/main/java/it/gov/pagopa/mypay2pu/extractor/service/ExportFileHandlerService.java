package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ExportFileHandlerService {

  private final DataExportFacadeService dataExportFacadeService;
  private final ExportFileStatusService exportFileStatusService;
  private final TaskExecutor extractorTaskExecutor;

  public ExportFileHandlerService(DataExportFacadeService dataExportFacadeService,
                                  ExportFileStatusService exportFileStatusService,
                                  @Qualifier("extractorTaskExecutor") TaskExecutor extractorTaskExecutor) {
    this.dataExportFacadeService = dataExportFacadeService;
    this.exportFileStatusService = exportFileStatusService;
    this.extractorTaskExecutor = extractorTaskExecutor;
  }

  public String createExtraction(ExtractionRequest request) {
    String extractionId = UUID.randomUUID().toString();
    extractorTaskExecutor.execute(() -> executeExport(extractionId, request));
    return extractionId;
  }

  public void executeExport(String extractionId, ExtractionRequest request) {
    log.info("Processing extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());

    exportFileStatusService.createNew(extractionId, request);

    ExportFileResult exportFileResult;
    try {
      exportFileResult = dataExportFacadeService.executeExport(extractionId, request);
    } catch (Exception e) {
      log.error("Error processing extraction {}", extractionId, e);
      exportFileResult = new ExportFileResult(List.of(), List.of(), e.getMessage());
    }
    exportFileStatusService.update(extractionId, exportFileResult);
  }
}
