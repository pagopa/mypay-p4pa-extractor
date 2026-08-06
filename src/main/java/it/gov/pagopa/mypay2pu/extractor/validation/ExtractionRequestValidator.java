package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Base validator for extraction requests and extraction identifiers.
 */
@Component
public class ExtractionRequestValidator {

  public void validate(ExtractionRequest request) {
    if (request != null) {
      validateFilters(request.getFilters());
    }
  }

  protected void validateFilters(ExtractionFilters filters) {
    if (filters != null) {
      validateInterval(filters.getDateFrom(), filters.getDateTo());
    }
  }

  protected void validateInterval(LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new BadRequestException("INVALID_EXTRACTION_FILTERS",
        "filters.dateFrom must be before or equal to filters.dateTo");
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
