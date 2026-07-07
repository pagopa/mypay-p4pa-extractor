package it.gov.pagopa.mypay2pu.extractor.service.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatingCsvSupplierTest {

  @Test
  void testValidatingSupplier_validRow() {
    // Given
    TestDto validRow = new TestDto("Name", "name@example.com", 10);
    List<TestDto> data = List.of(validRow);

    AtomicBoolean called = new AtomicBoolean(false);
    Supplier<List<TestDto>> source = () -> {
      if (!called.getAndSet(true)) {
        return data;
      }
      return Collections.emptyList();
    };

    var errorCollector = new ValidationErrorCollector(null);
    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();

    // Then
    assertEquals(1, result.size());
    assertEquals("Name", result.get(0).getName());
    assertEquals(0, errorCollector.getErrors().size());
  }

  @Test
  void testValidatingSupplier_invalidRow() {
    // Given
    TestDto invalidRow = new TestDto("", "invalid-email", -5);
    List<TestDto> data = List.of(invalidRow);
    AtomicBoolean called = new AtomicBoolean(false);

    Supplier<List<TestDto>> source = () -> {
      if (!called.getAndSet(true)) {
        return data;
      }
      return Collections.emptyList();
    };

    var errorCollector = new ValidationErrorCollector(null);
    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();

    // Then
    assertEquals(0, result.size());
    assertEquals(3, errorCollector.getErrors().size());

    // Errors should be sorted by field name
    var errors = errorCollector.getErrors();
    assertTrue(errors.stream().anyMatch(e -> e.field().equals("email")));
    assertTrue(errors.stream().anyMatch(e -> e.field().equals("name")));
    assertTrue(errors.stream().anyMatch(e -> e.field().equals("value")));
  }

  @Test
  void testValidatingSupplier_mixedRows() {
    // Given
    TestDto validRow1 = new TestDto("Name", "name@example.com", 10);
    TestDto validRow2 = new TestDto("Name 2", "name2@example.com", 20);
    TestDto invalidRow = new TestDto("", "invalid-email", -5);
    TestDto validRow3 = new TestDto("Name 3", "name3@example.com", 15);

    List<List<TestDto>> batches = List.of(
      List.of(validRow1, invalidRow, validRow2),
      List.of(validRow3),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new ValidationErrorCollector(null);
    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When - First batch: validRow1, validRow2 (invalidRow filtered)
    List<TestDto> result1 = supplier.get();
    // When - Second batch: validRow3
    List<TestDto> result2 = supplier.get();
    // When - Third batch: empty
    List<TestDto> result3 = supplier.get();

    // Then
    assertEquals(2, result1.size());
    assertEquals("Name", result1.get(0).getName());
    assertEquals("Name 2", result1.get(1).getName());

    assertEquals(1, result2.size());
    assertEquals("Name 3", result2.get(0).getName());

    assertEquals(0, result3.size());

    // Should have 3 errors from invalidRow
    assertEquals(3, errorCollector.getErrors().size());
  }

  @Test
  void testValidatingSupplier_rowNumbering() {
    // Given - Multiple batches to test row number tracking
    TestDto row1 = new TestDto("", "invalid1", -1);  // row 2: invalid
    TestDto row2 = new TestDto("Name", "name@example.com", 10);  // row 3: valid
    TestDto row3 = new TestDto("", "invalid2", -2);  // row 4: invalid
    TestDto row4 = new TestDto("Name 2", "name2@example.com", 20);  // row 5: valid

    List<List<TestDto>> batches = List.of(
      List.of(row1, row2),
      List.of(row3, row4),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new ValidationErrorCollector(null);
    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result1 = supplier.get();
    List<TestDto> result2 = supplier.get();

    // Then
    // First batch should contain only row2 (valid)
    assertEquals(1, result1.size());

    // Second batch should contain only row4 (valid)
    assertEquals(1, result2.size());

    // Errors should have correct row numbers
    var errors = errorCollector.getErrors();
    assertEquals(6, errors.size());  // 3 errors from row1 + 3 errors from row3

    // Check row numbers: row1 is row 2, row3 is row 4
    assertTrue(errors.stream()
      .filter(e -> e.rowNumber() == 2)
      .count() >= 1, "Should have errors from row 2");

    assertTrue(errors.stream()
      .filter(e -> e.rowNumber() == 4)
      .count() >= 1, "Should have errors from row 4");
  }

  @Test
  void testValidatingSupplier_emptyBatch() {
    // Given
    var errorCollector = new ValidationErrorCollector(null);
    Supplier<List<TestDto>> source = Collections::emptyList;

    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();

    // Then
    assertEquals(0, result.size());
    assertEquals(0, errorCollector.getErrors().size());
  }

  @Test
  void testValidatingSupplier_allRowsInvalid() {
    // Given
    TestDto invalid1 = new TestDto("", "", 0);
    TestDto invalid2 = new TestDto(null, "not-an-email", -10);

    List<List<TestDto>> batches = List.of(
      List.of(invalid1, invalid2),
      Collections.emptyList()
    );

    var batchIterator = batches.iterator();
    Supplier<List<TestDto>> source = () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();

    var errorCollector = new ValidationErrorCollector(null);
    var supplier = new ValidatingCsvSupplier<>(source,
      Validation.buildDefaultValidatorFactory().getValidator(),
      errorCollector);

    // When
    List<TestDto> result = supplier.get();

    // Then
    assertEquals(0, result.size());
    assertTrue(errorCollector.getErrors().size() > 0);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class TestDto {
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
