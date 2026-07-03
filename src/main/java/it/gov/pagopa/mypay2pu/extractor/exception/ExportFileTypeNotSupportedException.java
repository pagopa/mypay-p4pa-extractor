package it.gov.pagopa.mypay2pu.extractor.exception;

public class ExportFileTypeNotSupportedException extends BaseBusinessException {
  public ExportFileTypeNotSupportedException(String message) {
    super("EXPORT_FILE_NOT_SUPPORTED", message);
  }
}
