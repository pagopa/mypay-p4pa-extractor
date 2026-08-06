package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.springframework.stereotype.Component;

/**
 * Routes extraction validation to the validator matching the requested file type.
 */
@Component
public class ExtractionValidationFacade {

  private final ExtractionRequestValidator extractionRequestValidator;
  private final CsvLogicalKeyValidator csvLogicalKeyValidator;
  private final PairedLogicalKeyValidator pairedLogicalKeyValidator;

  public ExtractionValidationFacade(ExtractionRequestValidator extractionRequestValidator,
                                    CsvLogicalKeyValidator csvLogicalKeyValidator,
                                    PairedLogicalKeyValidator pairedLogicalKeyValidator) {
    this.extractionRequestValidator = extractionRequestValidator;
    this.csvLogicalKeyValidator = csvLogicalKeyValidator;
    this.pairedLogicalKeyValidator = pairedLogicalKeyValidator;
  }

  public void validate(ExtractionRequest request) {
    if (request == null) {
      throw new BadRequestException("INVALID_EXTRACTION_REQUEST", "fileTypes must be provided");
    }

    switch (request.getFileTypes()) {
      case ORGANIZATIONS, ORG_SIL_SERVICES -> extractionRequestValidator.validate(request);
      case DEBT_POSITIONS_TYPE, DEBT_POSITIONS_TYPE_ORG -> csvLogicalKeyValidator.validate(request);
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS, DEBT_POSITIONS ->
        pairedLogicalKeyValidator.validate(request);
      default -> throw new ExportFileTypeNotSupportedException(
        "Invalid export file type: " + request.getFileTypes()
      );
    }
  }
}
