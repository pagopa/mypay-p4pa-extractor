package it.gov.pagopa.mypay2pu.extractor.exception;

/**
 * Signals a recoverable row-mapping failure. The export pipeline records it in the CSV error
 * report and skips only the affected row.
 */
public class CsvRowMappingException extends IllegalArgumentException {

  private final String field;
  private final String rejectedValue;

  public CsvRowMappingException(String field, String rejectedValue, String message, Throwable cause) {
    super(message, cause);
    this.field = field;
    this.rejectedValue = rejectedValue;
  }

  public String getField() {
    return field;
  }

  public String getRejectedValue() {
    return rejectedValue;
  }
}