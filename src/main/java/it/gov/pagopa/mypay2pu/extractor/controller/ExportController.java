package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ExtractApi;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionAcceptedResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.enums.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.dto.ExtractionFiltersDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.ExtractionRequestDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class ExportController implements ExtractApi {


  @Override
  public ResponseEntity<ExtractionAcceptedResponse> createExtraction(@Valid ExtractionRequest extractionRequest) {
    log.info("createExtraction: ipaCode={}, fileTypes={}", extractionRequest.getIpaCode(), extractionRequest.getFileTypes());
    ExtractionRequestDTO request = toInternalRequest(extractionRequest);
    MigrationFileType fileType = request.getFileType();
    if (fileType != MigrationFileType.ORGANIZATIONS) {
      throw new BadRequestException("UNSUPPORTED_FILE_TYPE", "Current POC supports only ORGANIZATIONS");
    }

    UUID extractionId = UUID.randomUUID();
    //TODO extraction will be implemented after resolution of tasks P4ADEV-4811 and P4ADEV-4816
    log.info("Accepted extraction {} for organization {} and fileType {}", extractionId, request.getIpaCode(), fileType);
    return ResponseEntity.accepted().body(new ExtractionAcceptedResponse(extractionId));
  }

  @Override
  public ResponseEntity<ExtractionStatusResponse> getExtractionStatus(String extractionId) {
    log.info("getExtractionStatus: extractionId={}", extractionId);
    //TODO extraction status will be implemented after resolution of tasks P4ADEV-4816
    return ResponseEntity.ok(null);
  }

  private ExtractionRequestDTO toInternalRequest(ExtractionRequest request) {
    return new ExtractionRequestDTO(
      request.getIpaCode(),
      request.getFileTypes(),
      toInternalFilters(request.getFilters())
    );
  }

  private ExtractionFiltersDTO toInternalFilters(ExtractionFilters filters) {
    if (filters == null) {
      return null;
    }
    return new ExtractionFiltersDTO(filters.getModifiedFrom(), filters.getModifiedTo());
  }

}
