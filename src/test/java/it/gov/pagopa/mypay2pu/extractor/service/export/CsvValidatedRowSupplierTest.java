package it.gov.pagopa.mypay2pu.extractor.service.export;

import com.opencsv.bean.CsvBindByName;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvValidatedRowSupplierTest {
  private final CsvService csvService = new CsvService(';', '"');

  @TempDir
  Path tempDir;

  @Test
  void givenValidRowWhenGetThenReturnValidRowWithoutErrors() throws IOException {
    // Given
    TestDto validRow = new TestDto("Name", "name@example.com", 10);
    List<TestDto> data = List.of(validRow);
    Path csvFile = tempDir.resolve("valid-row.csv");

    AtomicBoolean called = new AtomicBoolean(false);
    Supplier<List<TestDto>> source = () -> {
      if (!called.getAndSet(true)) {
        return data;
      }
      return Collections.emptyList();
    };

    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    assertEquals(1, result.size());
    assertEquals("Name", result.get(0).getName());
    assertEquals(0, errorRows.size());
  }

  @Test
  void givenInvalidRowWhenGetThenFilterRowAndCollectErrors() throws IOException {
    // Given
    TestDto invalidRow = new TestDto("", "invalid-email", -5);
    List<TestDto> data = List.of(invalidRow);
    Path csvFile = tempDir.resolve("invalid-row.csv");
    AtomicBoolean called = new AtomicBoolean(false);

    Supplier<List<TestDto>> source = () -> {
      if (!called.getAndSet(true)) {
        return data;
      }
      return Collections.emptyList();
    };

    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    assertEquals(0, result.size());
    assertEquals(3, errorRows.size());

    // Errors should be sorted by field name
    assertTrue(errorRows.stream().anyMatch(e -> e.contains("email")));
    assertTrue(errorRows.stream().anyMatch(e -> e.contains("name")));
    assertTrue(errorRows.stream().anyMatch(e -> e.contains("value")));
  }

  @Test
  void givenMixedRowsWhenGetThenReturnOnlyValidRowsAndCollectErrors() throws IOException {
    // Given
    TestDto validRow1 = new TestDto("Name", "name@example.com", 10);
    TestDto validRow2 = new TestDto("Name 2", "name2@example.com", 20);
    TestDto invalidRow = new TestDto("", "invalid-email", -5);
    TestDto validRow3 = new TestDto("Name 3", "name3@example.com", 15);
    Path csvFile = tempDir.resolve("mixed-rows.csv");

    List<List<TestDto>> batches = List.of(
      List.of(validRow1, invalidRow, validRow2),
      List.of(validRow3),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When - First batch: validRow1, validRow2 (invalidRow filtered)
    List<TestDto> result1 = supplier.get();
    // When - Second batch: validRow3
    List<TestDto> result2 = supplier.get();
    // When - Third batch: empty
    List<TestDto> result3 = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    assertEquals(2, result1.size());
    assertEquals("Name", result1.get(0).getName());
    assertEquals("Name 2", result1.get(1).getName());

    assertEquals(1, result2.size());
    assertEquals("Name 3", result2.get(0).getName());

    assertEquals(0, result3.size());

    // Should have 3 errors from invalidRow
    assertEquals(3, errorRows.size());
  }

  @Test
  void givenMultipleBatchesWhenGetThenKeepCorrectCsvRowNumbersInErrors() throws IOException {
    // Given - Multiple batches to test row number tracking
    TestDto row1 = new TestDto("", "invalid1", -1);  // row 2: invalid
    TestDto row2 = new TestDto("Name", "name@example.com", 10);  // row 3: valid
    TestDto row3 = new TestDto("", "invalid2", -2);  // row 4: invalid
    TestDto row4 = new TestDto("Name 2", "name2@example.com", 20);  // row 5: valid
    Path csvFile = tempDir.resolve("row-numbering.csv");

    List<List<TestDto>> batches = List.of(
      List.of(row1, row2),
      List.of(row3, row4),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result1 = supplier.get();
    List<TestDto> result2 = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    // First batch should contain only row2 (valid)
    assertEquals(1, result1.size());

    // Second batch should contain only row4 (valid)
    assertEquals(1, result2.size());

    // Errors should have correct row numbers
    assertEquals(6, errorRows.size());  // 3 errors from row1 + 3 errors from row3

    // Check row numbers: row1 is row 2, row3 is row 4
    assertTrue(errorRows.stream()
      .map(this::extractRowNumber)
      .filter(rowNumber -> rowNumber == 2)
      .count() >= 1, "Should have errors from row 2");

    assertTrue(errorRows.stream()
      .map(this::extractRowNumber)
      .filter(rowNumber -> rowNumber == 4)
      .count() >= 1, "Should have errors from row 4");
  }

  @Test
  void givenEmptyBatchWhenGetThenReturnEmptyWithoutErrors() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("empty-batch.csv");
    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    Supplier<List<TestDto>> source = Collections::emptyList;

    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    assertEquals(0, result.size());
    assertEquals(0, errorRows.size());
  }

  @Test
  void givenAllRowsInvalidWhenGetThenReturnEmptyAndCollectErrors() throws IOException {
    // Given
    TestDto invalid1 = new TestDto("", "", 0);
    TestDto invalid2 = new TestDto(null, "not-an-email", -10);
    Path csvFile = tempDir.resolve("all-invalid.csv");

    List<List<TestDto>> batches = List.of(
      List.of(invalid1, invalid2),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = new CsvValidatedRowSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();
    List<String> errorRows = readErrorRows(errorCollector, csvFile);

    // Then
    assertEquals(0, result.size());
    assertTrue(errorRows.size() > 0);
  }

  private List<String> readErrorRows(CsvRowErrorCollector errorCollector, Path csvFile) throws IOException {
    var errorPath = errorCollector.writeToFile(csvFile);
    if (errorPath.isEmpty()) {
      return List.of();
    }
    return Files.readAllLines(errorPath.get(), StandardCharsets.UTF_8).stream().skip(1).toList();
  }

  private long extractRowNumber(String csvErrorRow) {
    String normalizedRow = csvErrorRow.replace("\"", "");
    return Long.parseLong(normalizedRow.split(";", 2)[0]);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class TestDto implements CsvExportDto {
    @CsvBindByName
    @NotBlank(message = "name must not be blank")
    private String name;

    @CsvBindByName
    @Email(message = "email must be valid")
    private String email;

    @CsvBindByName
    @Positive(message = "value must be positive")
    private Integer value;
  }
}
