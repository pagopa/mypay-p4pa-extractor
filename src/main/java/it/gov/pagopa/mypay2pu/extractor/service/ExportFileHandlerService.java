package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionRequestValidator;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionValidationFacade;
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
  private final ExtractionValidationFacade extractionValidationFacade;
  private final ExtractionRequestValidator extractionRequestValidator;

  public ExportFileHandlerService(DataExportFacadeService dataExportFacadeService,
                                  ExportFileStatusService exportFileStatusService,
                                  @Qualifier("extractorTaskExecutor") TaskExecutor extractorTaskExecutor,
                                  ExtractionValidationFacade extractionValidationFacade,
                                  ExtractionRequestValidator extractionRequestValidator) {
    this.dataExportFacadeService = dataExportFacadeService;
    this.exportFileStatusService = exportFileStatusService;
    this.extractorTaskExecutor = extractorTaskExecutor;
    this.extractionValidationFacade = extractionValidationFacade;
    this.extractionRequestValidator = extractionRequestValidator;
  }

  public String createExtraction(ExtractionRequest request) {
    extractionValidationFacade.validate(request);
    String extractionId = UUID.randomUUID().toString();
    extractorTaskExecutor.execute(() -> executeExport(extractionId, request));
    return extractionId;
  }

  public ExtractionStatusResponse getExtractionStatus(String extractionId) {
    extractionRequestValidator.validateExtractionId(extractionId);
    return exportFileStatusService.readStatus(extractionId);
  }

  public void executeExport(String extractionId, ExtractionRequest request) {
    log.info("Processing extraction {} for ipaCodes {} and fileTypes {}", extractionId, request.getIpaCodes(), request.getFileTypes());

    exportFileStatusService.createNew(extractionId, request);

    ExportFileResult exportFileResult;
    try {
      exportFileResult = dataExportFacadeService.executeExport(extractionId, request);
    } catch (Exception e) {
      log.error("Error processing extraction {}", extractionId, e);
      exportFileResult = new ExportFileResult(List.of(), e.getMessage());
    }
    exportFileStatusService.update(extractionId, exportFileResult);
  }
}
