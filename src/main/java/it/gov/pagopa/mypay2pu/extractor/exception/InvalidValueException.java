package it.gov.pagopa.mypay2pu.extractor.exception;

public class InvalidValueException extends BaseBusinessException {

  public InvalidValueException(String code, String message) {
    super(code, message);
  }
}
