package it.gov.pagopa.mypay2pu.extractor.exception;

public class ExportFileNotFoundException extends BaseBusinessException {

    public ExportFileNotFoundException(String message) {
        super("EXPORT_FILE_NOT_FOUND", message);
    }
}
