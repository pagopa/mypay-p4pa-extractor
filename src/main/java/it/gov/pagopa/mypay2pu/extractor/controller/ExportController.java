package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ExportController implements ExtractApi {

  private final ExportFileHandlerService exportFileHandlerService;

  public ExportController(ExportFileHandlerService exportFileHandlerService) {
    this.exportFileHandlerService = exportFileHandlerService;
  }

  @Override
  public ResponseEntity<ExtractionAcceptedResponse> createExtraction(@Valid ExtractionRequest request) {
    log.info("createExtraction: ipaCodes={}, fileTypes={}", request.getIpaCodes(), request.getFileTypes());

    String extractionId = exportFileHandlerService.createExtraction(request);

    log.info("Accepted extraction {} for ipaCodes {} and fileTypes {}", extractionId, request.getIpaCodes(), request.getFileTypes());
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    log.info("getExtractionStatus: extractionId={}", extractionId);
    return ResponseEntity.ok(exportFileHandlerService.getExtractionStatus(extractionId));
  }
}
