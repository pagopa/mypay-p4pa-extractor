package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataExportFacadeServiceTest {

  private final DataExportFacadeService service = new DataExportFacadeService();

  @Test
  void executeExportShouldReturnEmptyOrganizationsResult() {
    ExportFileResult result = service.executeExport("extraction-id", MigrationFileType.ORGANIZATIONS);

    assertEquals(List.of(), result.files());
    assertNull(result.error());
  }

  @Test
  void executeExportShouldRejectUnsupportedFileType() {
    ExportFileTypeNotSupportedException exception = assertThrows(
      ExportFileTypeNotSupportedException.class,
      () -> service.executeExport("extraction-id", MigrationFileType.DEBT_POSITIONS)
    );

    assertEquals("EXPORT_FILE_NOT_SUPPORTED", exception.getCode());
    assertEquals("Invalid export file type: DEBT_POSITIONS", exception.getMessage());
  }
}
