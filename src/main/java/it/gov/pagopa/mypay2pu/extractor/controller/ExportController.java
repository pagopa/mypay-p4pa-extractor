package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileStatusService;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionRequestValidator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ExportController implements ExtractApi {

  private final ExportFileHandlerService exportFileHandlerService;
  private final ExportFileStatusService exportFileStatusService;
  private final ExtractionRequestValidator extractionRequestValidator;

  public ExportController(ExportFileHandlerService exportFileHandlerService,
                          ExportFileStatusService exportFileStatusService,
                          ExtractionRequestValidator extractionRequestValidator) {
    this.exportFileHandlerService = exportFileHandlerService;
    this.exportFileStatusService = exportFileStatusService;
    this.extractionRequestValidator = extractionRequestValidator;
  }

  @Override
  public ResponseEntity<ExtractionAcceptedResponse> createExtraction(@Valid ExtractionRequest request) {
    extractionRequestValidator.validate(request);
    log.info("createExtraction: ipaCode={}, fileTypes={}", request.getIpaCode(), request.getFileTypes());

    String extractionId = exportFileHandlerService.createExtraction(request);

    log.info("Accepted extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    extractionRequestValidator.validateExtractionId(extractionId);
    log.info("getExtractionStatus: extractionId={}", extractionId);
    return ResponseEntity.ok(exportFileStatusService.readStatus(extractionId));
  }
}
