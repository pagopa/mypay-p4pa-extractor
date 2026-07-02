package it.gov.pagopa.mypay2pu.extractor.exception;


public class InvalidCsvRowException extends BaseBusinessException {

  /**
   * Constructs a new InvalidCsvRowException with the specified detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
   */
  public InvalidCsvRowException(String message) {
    super("INVALID_CSV_ROW", message);
  }
}
