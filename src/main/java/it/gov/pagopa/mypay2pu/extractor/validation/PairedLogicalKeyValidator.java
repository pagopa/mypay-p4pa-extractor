package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.utils.Constants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates extraction requests whose logical key is composed of two comma-separated lists.
 */
@Component
public class PairedLogicalKeyValidator extends ExtractionRequestValidator {

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

  public static LogicalKeyPair parseLogicalKey(String logicalKey) {
    if (logicalKey == null) {
      return new LogicalKeyPair(List.of(), List.of());
    }
    if (logicalKey.isBlank()) {
      throw new IllegalArgumentException("filters.logicalKey must contain two non-empty comma-separated lists");
    }

    String[] parts = logicalKey.split(Pattern.quote(Constants.LOGICAL_KEY_PAIR_SEPARATOR), -1);
    if (parts.length != 2) {
      throw new IllegalArgumentException("filters.logicalKey must contain exactly one vertical bar");
    }
    return new LogicalKeyPair(
      CsvLogicalKeyValidator.parseLogicalKey(parts[0]),
      CsvLogicalKeyValidator.parseLogicalKey(parts[1])
    );
  }
}
