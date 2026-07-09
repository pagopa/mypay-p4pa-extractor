package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvRowErrorCollectorTest {

  private final CsvService csvService = new CsvService(';', '"');

  @TempDir
  Path tempDir;

  @Test
  void testWriteToFile_noErrors() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService);

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isEmpty());
    assertFalse(Files.exists(tempDir.resolve("output.errors.csv")));
  }

  @Test
  void testWriteToFile_withErrors() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService);
    collector.add(2, "email", "Email", "must be a well-formed email address", "invalid-email");
    collector.add(3, "name", "NotBlank", "must not be blank", "");

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isPresent());
    Path errorFile = result.get();
    assertEquals(tempDir.resolve("output.errors.csv"), errorFile);
    assertTrue(Files.exists(errorFile));

    String content = Files.readString(errorFile, StandardCharsets.UTF_8);
    String[] lines = content.split("\n");

    // Header + 2 error rows
    assertEquals(3, lines.length);
    assertTrue(lines[0].contains("rowNumber") && lines[0].contains("field"));
    assertTrue(lines[1].contains("2") && lines[1].contains("email"));
    assertTrue(lines[2].contains("3") && lines[2].contains("name"));
  }

  @Test
  void testWriteToFile_csvPathWithoutExtension() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService);
    collector.add(2, "email", "Email", "must be a well-formed email address", "invalid-email");

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isPresent());
    Path errorFile = result.get();
    assertEquals(tempDir.resolve("output.errors.csv"), errorFile);
    assertTrue(Files.exists(errorFile));
  }

  @Test
  void testWriteToFile_differentCsvPath() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("subdir").resolve("data.csv");
    Files.createDirectories(csvFile.getParent());

    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService);
    collector.add(2, "field1", "NotNull", "must not be null", "");

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isPresent());
    Path errorFile = result.get();
    assertEquals(tempDir.resolve("subdir").resolve("data.errors.csv"), errorFile);
    assertTrue(Files.exists(errorFile));
  }

  @Test
  void testWriteToFile_manyErrors() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService);

    for (int i = 2; i <= 10; i++) {
      collector.add(i, "field" + i, "Code" + i, "Error message " + i, "value" + i);
    }

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isPresent());
    Path errorFile = result.get();
    assertTrue(Files.exists(errorFile));

    String content = Files.readString(errorFile, StandardCharsets.UTF_8);
    String[] lines = content.split("\n");

    // Header + 9 error rows
    assertEquals(10, lines.length);
  }

  @Test
  void testWriteToFile_incrementalMode() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService, csvFile);
    collector.add(2, "email", "Email", "must be a well-formed email address", "invalid-email");
    collector.add(3, "name", "NotBlank", "must not be blank", "");

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isPresent());
    Path errorFile = result.get();
    assertEquals(tempDir.resolve("output.errors.csv"), errorFile);
    assertTrue(Files.exists(errorFile));

    String content = Files.readString(errorFile, StandardCharsets.UTF_8);
    String[] lines = content.split("\n");
    assertEquals(3, lines.length);
    assertTrue(lines[1].contains("2") && lines[1].contains("email"));
    assertTrue(lines[2].contains("3") && lines[2].contains("name"));
  }

  @Test
  void testWriteToFile_incrementalModeUsesConstructorPath() throws IOException {
    // Given
    Path constructorCsvFile = tempDir.resolve("constructor.csv");
    Path ignoredMethodCsvFile = tempDir.resolve("ignored.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService, constructorCsvFile);
    collector.add(2, "email", "Email", "must be a well-formed email address", "invalid-email");

    // When
    var result = collector.writeToFile(ignoredMethodCsvFile);

    // Then
    assertTrue(result.isPresent());
    assertEquals(tempDir.resolve("constructor.errors.csv"), result.get());
    assertFalse(Files.exists(tempDir.resolve("ignored.errors.csv")));
  }

  @Test
  void testWriteToFile_incrementalModeOverwritesExistingFile() throws IOException {
    // Given - existing error file with stale data from a previous run
    Path csvFile = tempDir.resolve("output.csv");
    Path errorFile = tempDir.resolve("output.errors.csv");
    Files.writeString(errorFile,
      "\"rowNumber\";\"field\";\"code\";\"message\";\"rejectedValue\"\n" +
      "\"2\";\"email\";\"Email\";\"must be a well-formed email address\";\"bad\"\n",
      StandardCharsets.UTF_8);

    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService, csvFile);
    collector.add(3, "name", "NotBlank", "must not be blank", "");

    // When
    var result = collector.writeToFile(csvFile);

    // Then - stale row from previous run must not appear; only header + new row
    assertTrue(result.isPresent());
    String content = Files.readString(errorFile, StandardCharsets.UTF_8);
    String[] lines = content.split("\n");
    assertEquals(2, lines.length);
    assertTrue(lines[0].contains("rowNumber"));
    assertTrue(lines[1].contains("name"));
    assertFalse(content.contains("email"));
  }

  @Test
  void testWriteToFile_incrementalModeNoErrorsDeletesErrorFile() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    Path errorFile = tempDir.resolve("output.errors.csv");
    Files.writeString(errorFile, "rowNumber;field;code;message;rejectedValue\n", StandardCharsets.UTF_8);
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService, csvFile);

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isEmpty());
    assertFalse(Files.exists(errorFile));
  }

  @Test
  void testWriteToFile_incrementalModeNoErrorsDoesNotCreateErrorFile() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("output.csv");
    Path errorFile = tempDir.resolve("output.errors.csv");
    CsvRowErrorCollector collector = new CsvRowErrorCollector(csvService, csvFile);

    // When
    var result = collector.writeToFile(csvFile);

    // Then
    assertTrue(result.isEmpty());
    assertFalse(Files.exists(errorFile));
  }
}
