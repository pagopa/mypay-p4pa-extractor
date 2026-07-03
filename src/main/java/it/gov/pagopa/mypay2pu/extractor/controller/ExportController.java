package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class ExportController implements ExtractApi {

  private final ExportFileHandlerService exportFileHandlerService;

  public ExportController(ExportFileHandlerService exportFileHandlerService) {
    this.exportFileHandlerService = exportFileHandlerService;
  }

  @Override
  public ResponseEntity<ExtractionAcceptedResponse> createExtraction(@Valid ExtractionRequest request) {
    log.info("createExtraction: ipaCode={}, fileTypes={}", request.getIpaCode(), request.getFileTypes());

    String extractionId = UUID.randomUUID().toString();
    exportFileHandlerService.executeExport(extractionId, request);

    log.info("Accepted extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    log.info("getExtractionStatus: extractionId={}", extractionId);
    //TODO Extraction status will be implemented by task P4ADEV-4816
    throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_IMPLEMENTED,
      "Extraction status endpoint is not implemented yet");
  }
}
