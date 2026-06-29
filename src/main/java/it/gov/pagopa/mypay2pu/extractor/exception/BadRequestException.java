package it.gov.pagopa.mypay2pu.extractor.exception;

public class BadRequestException extends BaseBusinessException {

  public BadRequestException(String code, String message) {
    super(code, message);
  }
}
