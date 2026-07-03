package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataExportFacadeService {

  public ExportFileResult executeExport(String extractionId, MigrationFileType fileType) {
    return switch (fileType) {
      //TODO the extraction logic for type ORGANIZATIONS will be implemented by task P4ADEV-4810
      case ORGANIZATIONS -> new ExportFileResult(List.of(), null);
      default ->
        throw new ExportFileTypeNotSupportedException("Invalid export file type: " + fileType);
    };
  }
}
