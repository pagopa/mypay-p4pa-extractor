package it.gov.pagopa.mypay2pu.extractor.service.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipFileServiceTest {

  @TempDir
  Path tempDir;

  private final ZipFileService zipFileService = new ZipFileService();

  @Test
  void givenInputFilesWhenZipperThenCreateZipWithEntries() throws IOException {
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Files.writeString(file1, "content-1");
    Files.writeString(file2, "content-2");
    Path zipPath = tempDir.resolve("output.zip");

    zipFileService.zipper(zipPath, List.of(file1, file2));

    assertTrue(Files.exists(zipPath));
    Map<String, String> entries = unzipEntries(zipPath);
    assertEquals(2, entries.size());
    assertEquals("content-1", entries.get("file1.txt"));
    assertEquals("content-2", entries.get("file2.txt"));
  }

  @Test
  void givenMissingSourceFileWhenZipperThenThrowIllegalStateException() {
    Path zipPath = tempDir.resolve("output.zip");
    Path missingFile = tempDir.resolve("missing.txt");

    assertThrows(IllegalStateException.class, () -> zipFileService.zipper(zipPath, List.of(missingFile)));
  }

  private Map<String, String> unzipEntries(Path zipPath) throws IOException {
    Map<String, String> entries = new HashMap<>();
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        entries.put(entry.getName(), new String(zis.readAllBytes()));
      }
    }
    return entries;
  }
}
