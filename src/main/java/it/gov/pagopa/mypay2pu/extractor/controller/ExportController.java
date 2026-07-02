package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    MigrationFileType fileTypes = request.getFileTypes();
    if (fileTypes != MigrationFileType.ORGANIZATIONS) {
      throw new BadRequestException("UNSUPPORTED_FILE_TYPE", "Current POC supports only ORGANIZATIONS");
    }

    String extractionId = UUID.randomUUID().toString();
    exportFileHandlerService.executeExport(extractionId, request);
    log.info("Accepted extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    log.info("getExtractionStatus: extractionId={}", extractionId);
    //TODO extraction status will be implemented after resolution of tasks P4ADEV-4816
    return ResponseEntity.ok(null);
  }

}
