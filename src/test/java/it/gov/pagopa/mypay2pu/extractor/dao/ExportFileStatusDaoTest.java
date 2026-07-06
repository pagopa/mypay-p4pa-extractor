package it.gov.pagopa.mypay2pu.extractor.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportFileStatusDaoTest {

  @TempDir
  Path tempDir;

  private final ObjectMapper objectMapper = new JsonConfig().objectMapper();

  @Test
  void givenStatusWhenWriteAndReadThenReturnStoredValue() {
    ExportFileStatusDao service = new ExportFileStatusDao(
      objectMapper,
      tempDir.toString()
    );

    OffsetDateTime now = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    ExtractionStatusResponse status = new ExtractionStatusResponse(
      "extraction-id",
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      now,
      now,
      null,
      List.of("organizations.csv")
    );

    service.writeStatus(status);

    ExtractionStatusResponse storedStatus = service.readStatus("extraction-id");
    assertEquals("extraction-id", storedStatus.getExtractionId());
    assertEquals("IPA_CODE_TEST", storedStatus.getIpaCode());
    assertEquals(MigrationFileType.ORGANIZATIONS, storedStatus.getFileTypes());
    assertEquals(ExtractionStatus.RUNNING, storedStatus.getStatus());
    assertEquals(List.of("organizations.csv"), storedStatus.getFiles());
    assertNotNull(storedStatus.getCreatedAt());
    assertNotNull(storedStatus.getUpdatedAt());
  }

  @Test
  void givenMissingStatusFileWhenReadStatusThenThrowExportFileNotFoundException() {
    ExportFileStatusDao service = new ExportFileStatusDao(
      objectMapper,
      tempDir.toString()
    );

    ExportFileNotFoundException exception = assertThrows(
      ExportFileNotFoundException.class,
      () -> service.readStatus("missing-extraction-id")
    );

    assertEquals("EXPORT_FILE_NOT_FOUND", exception.getCode());
    assertEquals("File for extractionId: missing-extraction-id not found", exception.getMessage());
  }

  @Test
  void givenMalformedStatusFileWhenReadStatusThenThrowUncheckedIOException() throws Exception {
    ExportFileStatusDao service = new ExportFileStatusDao(
      objectMapper,
      tempDir.toString()
    );
    Path extractionDirectory = service.resolveExtractionDirectory("extraction-id");
    Files.createDirectories(extractionDirectory);
    Files.writeString(extractionDirectory.resolve("status.json"), "{malformed-json");

    UncheckedIOException exception = assertThrows(
      UncheckedIOException.class,
      () -> service.readStatus("extraction-id")
    );

    assertEquals("Cannot read status file for extraction extraction-id", exception.getMessage());
  }

}
