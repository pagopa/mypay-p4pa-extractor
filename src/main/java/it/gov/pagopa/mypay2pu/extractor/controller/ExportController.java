package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class ExportController implements ExtractApi {


  @Override
  public ResponseEntity<ExtractionAcceptedResponse> createExtraction(@Valid ExtractionRequest request) {
    log.info("createExtraction: ipaCode={}, fileTypes={}", request.getIpaCode(), request.getFileTypes());
    MigrationFileType fileTypes = request.getFileTypes();
    if (fileTypes != MigrationFileType.ORGANIZATIONS) {
      throw new BadRequestException("UNSUPPORTED_FILE_TYPE", "Current POC supports only ORGANIZATIONS");
    }

    UUID extractionId = UUID.randomUUID();
    //TODO extraction will be implemented after resolution of tasks P4ADEV-4811 and P4ADEV-4816
    log.info("Accepted extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), fileTypes);
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    log.info("getExtractionStatus: extractionId={}", extractionId);
    //TODO extraction status will be implemented after resolution of tasks P4ADEV-4816
    return ResponseEntity.ok(null);
  }

}
