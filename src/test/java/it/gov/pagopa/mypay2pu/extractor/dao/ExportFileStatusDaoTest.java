package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportFileStatusDaoTest {

  @TempDir
  Path tempDir;

  private final JsonMapper jsonMapper = new JsonConfig().objectMapperJackson3();

  @Test
  void givenStatusWhenWriteAndReadThenReturnStoredValue() {
    ExportFileStatusDao service = new ExportFileStatusDao(
      jsonMapper,
      tempDir.toString()
    );

    OffsetDateTime now = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    ExtractionStatusResponse status = new ExtractionStatusResponse(
      "extraction-id",
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.RUNNING,
      now,
      now,
      null,
      List.of(new ArchiveFile("archive1.zip", List.of("file1.csv", "file2.csv")))
    );

    service.writeStatus(status);

    ExtractionStatusResponse storedStatus = service.readStatus("extraction-id");
    assertEquals("extraction-id", storedStatus.getExtractionId());
    assertEquals(List.of("IPA_CODE_TEST"), storedStatus.getIpaCodes());
    assertEquals(MigrationFileType.ORGANIZATIONS, storedStatus.getFileTypes());
    assertEquals(ExtractionStatus.RUNNING, storedStatus.getStatus());
    assertEquals(
      List.of(new ArchiveFile("archive1.zip", List.of("file1.csv", "file2.csv"))),
      storedStatus.getArchiveFiles()
    );
    assertNotNull(storedStatus.getCreatedAt());
    assertNotNull(storedStatus.getUpdatedAt());
  }

  @Test
  void givenStatusWhenWrittenThenSerializeArchiveFilesWithNamesAndContents() throws Exception {
    ExportFileStatusDao service = new ExportFileStatusDao(jsonMapper, tempDir.toString());
    OffsetDateTime now = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    ExtractionStatusResponse status = new ExtractionStatusResponse(
      "extraction-id",
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.COMPLETED,
      now,
      now,
      null,
      List.of(
        new ArchiveFile("archive1.zip", List.of("file1.csv", "file2.csv")),
        new ArchiveFile("archive2.zip", List.of("file3.csv", "file4.csv"))
      )
    );

    service.writeStatus(status);

    String json = Files.readString(service.resolveExtractionDirectory("extraction-id").resolve("status.json"));
    assertTrue(json.contains("\"archiveFiles\""));
    assertTrue(json.contains("\"name\":\"archive1.zip\""));
    assertTrue(json.contains("\"files\":[\"file1.csv\",\"file2.csv\"]"));
    assertFalse(json.contains("\"files\":[\"archive1.zip\""));
  }

  @Test
  void givenMissingStatusFileWhenReadStatusThenThrowExportFileNotFoundException() {
    ExportFileStatusDao service = new ExportFileStatusDao(
      jsonMapper,
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
      jsonMapper,
      tempDir.toString()
    );
    Path extractionDirectory = service.resolveExtractionDirectory("extraction-id");
    Files.createDirectories(extractionDirectory);
    Files.writeString(extractionDirectory.resolve("status.json"), "{malformed-json");

    assertThrows(StreamReadException.class, () -> service.readStatus("extraction-id"));
  }

}
