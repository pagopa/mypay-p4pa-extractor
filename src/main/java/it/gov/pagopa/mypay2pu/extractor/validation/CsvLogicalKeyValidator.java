package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.utils.Constants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Validates extraction requests whose logical key is a single comma-separated list.
 */
@Component
public class CsvLogicalKeyValidator extends ExtractionRequestValidator {

  @Override
  public void validate(ExtractionRequest request) {
    super.validate(request);
    validateLogicalKey(request != null ? request.getFilters() : null);
  }

  protected void validateLogicalKey(ExtractionFilters filters) {
    String logicalKey = filters != null ? filters.getLogicalKey() : null;
    try {
      parseLogicalKey(logicalKey);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException("INVALID_EXTRACTION_FILTERS", exception.getMessage());
    }
  }

  public static List<String> parseLogicalKey(String logicalKey) {
    if (logicalKey == null) {
      return List.of();
    }
    if (logicalKey.isBlank() || logicalKey.contains(Constants.LOGICAL_KEY_PAIR_SEPARATOR)) {
      throw new IllegalArgumentException("filters.logicalKey must be a non-empty comma-separated list");
    }

    List<String> values = List.of(logicalKey.split(Constants.LOGICAL_KEY_VALUE_SEPARATOR, -1)).stream()
      .map(String::trim)
      .toList();
    if (values.stream().anyMatch(value -> !StringUtils.hasText(value))) {
      throw new IllegalArgumentException("filters.logicalKey must not contain empty values");
    }
    return values;
  }
}
