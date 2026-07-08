package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class ExtractionRequestValidator {

  public void validate(ExtractionRequest request) {
    if (request == null) {
      return;
    }

    ExtractionFilters filters = request.getFilters();
    if (filters == null) {
      return;
    }

    LocalDate modifiedFrom = filters.getModifiedFrom();
    LocalDate modifiedTo = filters.getModifiedTo();
    if (modifiedFrom != null && modifiedTo != null && modifiedFrom.isAfter(modifiedTo)) {
      throw new BadRequestException("INVALID_EXTRACTION_FILTERS",
        "filters.modifiedFrom must be before or equal to filters.modifiedTo");
    }
  }

  public void validateExtractionId(String extractionId) {
    try {
      UUID.fromString(extractionId);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new BadRequestException("INVALID_EXTRACTION_ID",
        "extractionId must be a valid UUID");
    }
  }
}
