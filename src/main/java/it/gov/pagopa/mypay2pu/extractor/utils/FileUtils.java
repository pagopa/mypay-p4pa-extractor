package it.gov.pagopa.mypay2pu.extractor.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class FileUtils {

  public static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private FileUtils() {}

  public static Path createWorkingDirectory(Path extractionDirectory, String prefix) {
    try {
      Files.createDirectories(extractionDirectory);
      return Files.createTempDirectory(extractionDirectory, prefix + "-");
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create working directory for " + prefix, e);
    }
  }

  public static void deleteRecursively(Path directory) {
    if (directory == null || !Files.exists(directory)) {
      return;
    }
    try (var paths = Files.walk(directory)) {
      paths.sorted(Comparator.reverseOrder())
        .forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            throw new IllegalStateException("Cannot clean temporary export directory " + directory, e);
          }
        });
    } catch (IOException e) {
      throw new IllegalStateException("Cannot clean temporary export directory " + directory, e);
    }
  }
}
