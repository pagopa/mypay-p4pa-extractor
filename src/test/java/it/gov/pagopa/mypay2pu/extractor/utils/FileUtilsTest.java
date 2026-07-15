package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

  @TempDir
  Path tempDir;

  @Test
  void givenValidDirectoryWhenCreateWorkingDirectoryThenCreateTempDirectory() {
    Path extractionDirectory = tempDir.resolve("extraction");
    String prefix = "payment";

    Path workingDirectory = FileUtils.createWorkingDirectory(extractionDirectory, prefix);

    assertTrue(Files.exists(workingDirectory));
    assertTrue(Files.isDirectory(workingDirectory));
    assertTrue(Files.exists(extractionDirectory));
    assertTrue(workingDirectory.getFileName().toString().startsWith(prefix + "-"));
    assertEquals(workingDirectory.getParent(), extractionDirectory);
  }

  @Test
  void givenNonExistentExtractionDirectoryWhenCreateWorkingDirectoryThenCreateBothDirectories() {
    Path extractionDirectory = tempDir.resolve("nested").resolve("extraction");
    String prefix = "test";

    Path workingDirectory = FileUtils.createWorkingDirectory(extractionDirectory, prefix);

    assertTrue(Files.exists(workingDirectory));
    assertTrue(Files.exists(extractionDirectory));
    assertEquals(workingDirectory.getParent(), extractionDirectory);
  }

  @Test
  void givenValidPathWithPrefixWhenCreateWorkingDirectoryThenDirectoryNameHasPrefix() {
    Path extractionDirectory = tempDir.resolve("extraction");
    String prefix = "myprefix";

    Path workingDirectory = FileUtils.createWorkingDirectory(extractionDirectory, prefix);

    assertTrue(workingDirectory.getFileName().toString().startsWith(prefix + "-"));
  }

  @Test
  void givenEmptyDirectoryWhenDeleteRecursivelyShouldDeleteDirectory() throws IOException {
    Path directoryToDelete = tempDir.resolve("empty-dir");
    Files.createDirectory(directoryToDelete);

    FileUtils.deleteRecursively(directoryToDelete);

    assertFalse(Files.exists(directoryToDelete));
  }

  @Test
  void givenDirectoryWithFileWhenDeleteRecursivelyShouldDeleteAll() throws IOException {
    Path directoryToDelete = tempDir.resolve("dir-with-file");
    Files.createDirectory(directoryToDelete);
    Path file = directoryToDelete.resolve("test.txt");
    Files.writeString(file, "test content");

    FileUtils.deleteRecursively(directoryToDelete);

    assertFalse(Files.exists(directoryToDelete));
    assertFalse(Files.exists(file));
  }

  @Test
  void givenNestedDirectoriesWithFilesWhenDeleteRecursivelyShouldDeleteAll() throws IOException {
    Path directoryToDelete = tempDir.resolve("nested-dir");
    Path subdir1 = directoryToDelete.resolve("sub1");
    Path subdir2 = subdir1.resolve("sub2");
    Files.createDirectories(subdir2);
    Files.writeString(subdir1.resolve("file1.txt"), "content1");
    Files.writeString(subdir2.resolve("file2.txt"), "content2");

    FileUtils.deleteRecursively(directoryToDelete);

    assertFalse(Files.exists(directoryToDelete));
    assertFalse(Files.exists(subdir1));
    assertFalse(Files.exists(subdir2));
  }

  @Test
  void givenNullPathWhenDeleteRecursivelyThenDoNothing() {
    assertDoesNotThrow(() -> FileUtils.deleteRecursively(null));
  }

  @Test
  void givenNonExistentPathWhenDeleteRecursivelyThenDoNothing() {
    Path nonExistentPath = tempDir.resolve("does-not-exist");

    assertDoesNotThrow(() -> FileUtils.deleteRecursively(nonExistentPath));
    assertFalse(Files.exists(nonExistentPath));
  }

  @Test
  void givenMultipleFilesAndDirectoriesWhenDeleteRecursivelyShouldDeleteAll() throws IOException {
    Path directoryToDelete = tempDir.resolve("complex-dir");
    Files.createDirectory(directoryToDelete);

    // Create multiple files and subdirectories
    for (int i = 0; i < 3; i++) {
      Files.writeString(directoryToDelete.resolve("file" + i + ".txt"), "content" + i);
      Path subdir = directoryToDelete.resolve("subdir" + i);
      Files.createDirectory(subdir);
      for (int j = 0; j < 2; j++) {
        Files.writeString(subdir.resolve("nested" + j + ".txt"), "nested-content");
      }
    }

    FileUtils.deleteRecursively(directoryToDelete);

    assertFalse(Files.exists(directoryToDelete));
  }

  @Test
  void verifyFileTimestampFormatterPattern() {
    DateTimeFormatter formatter = FileUtils.FILE_TIMESTAMP_FORMATTER;
    LocalDateTime testTime = LocalDateTime.of(2026, Month.JULY, 15, 17, 28, 43);

    String formatted = testTime.format(formatter);

    assertEquals("20260715172843", formatted);
  }

  @Test
  void fileTimestampFormatterShouldFormatCurrentTimeCorrectly() {
    DateTimeFormatter formatter = FileUtils.FILE_TIMESTAMP_FORMATTER;
    LocalDateTime now = LocalDateTime.now();

    String formatted = now.format(formatter);

    assertTrue(formatted.matches("\\d{14}"));
    assertEquals(14, formatted.length());
  }
}
