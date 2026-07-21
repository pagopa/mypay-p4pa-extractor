package it.gov.pagopa.mypay2pu.extractor.exception;

/**
 * Signals a row-mapping failure caused by an invalid source value during CSV export conversion.
 */
public class CsvRowMappingException extends IllegalArgumentException {

  private final String errorCode;
  private final String field;
  private final String rejectedValue;

  public CsvRowMappingException(
    String errorCode,
    String field,
    String rejectedValue,
    String message,
    Throwable cause
  ) {
    super(message, cause);
    this.errorCode = errorCode;
    this.field = field;
    this.rejectedValue = rejectedValue;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getField() {
    return field;
  }

  public String getRejectedValue() {
    return rejectedValue;
  }
}