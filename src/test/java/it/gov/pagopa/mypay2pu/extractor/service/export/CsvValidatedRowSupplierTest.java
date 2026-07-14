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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvValidatedRowSupplierTest {
  private final CsvService csvService = new CsvService(';', '"');

  @TempDir
  Path tempDir;

  @Test
  void get_fillsPageUsingMultipleSourceBatches() {
    Supplier<List<TestDto>> source = batches(
      List.of(valid("one"), valid("two")),
      List.of(valid("three"))
    );
    var supplier = supplier(source, 3, new CsvRowErrorCollector(csvService));

    assertEquals(List.of("one", "two", "three"), names(supplier.get()));
  }

  @Test
  void get_preservesBufferedRowsForTheNextPage() {
    Supplier<List<TestDto>> source = batches(List.of(valid("one"), valid("two"), valid("three")));
    var supplier = supplier(source, 2, new CsvRowErrorCollector(csvService));

    assertEquals(List.of("one", "two"), names(supplier.get()));
    assertEquals(List.of("three"), names(supplier.get()));
    assertTrue(supplier.get().isEmpty());
  }

  @Test
  void get_skipsEntirelyInvalidSourceBatches() throws IOException {
    Path csvFile = tempDir.resolve("invalid-rows.csv");
    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var supplier = supplier(batches(
      List.of(invalid(), invalid()),
      List.of(invalid())
    ), 2, errorCollector);

    assertTrue(supplier.get().isEmpty());
    assertEquals(9, readErrorRows(errorCollector, csvFile).size());
  }

  @Test
  void get_returnsFinalPartialPageAndHandlesEmptySource() {
    var supplier = supplier(batches(List.of(valid("one"), valid("two"), valid("three"))),
      2, new CsvRowErrorCollector(csvService));

    assertEquals(List.of("one", "two"), names(supplier.get()));
    assertEquals(List.of("three"), names(supplier.get()));
    assertTrue(supplier.get().isEmpty());
    assertTrue(supplier(Collections::emptyList, 2, new CsvRowErrorCollector(csvService)).get().isEmpty());
  }

  @Test
  void get_preservesOrderAndRecordsInvalidRowsOnce() throws IOException {
    Path csvFile = tempDir.resolve("mixed-rows.csv");
    var errorCollector = new CsvRowErrorCollector(csvService, csvFile);
    var sourceCalls = new AtomicInteger();
    Supplier<List<TestDto>> source = () -> switch (sourceCalls.getAndIncrement()) {
      case 0 -> List.of(valid("one"), invalid(), valid("two"));
      case 1 -> List.of(valid("three"));
      default -> Collections.emptyList();
    };
    var supplier = supplier(source, 2, errorCollector);

    assertEquals(List.of("one", "two"), names(supplier.get()));
    assertEquals(List.of("three"), names(supplier.get()));
    assertEquals(3, readErrorRows(errorCollector, csvFile).size());
  }

  private Supplier<List<TestDto>> supplier(
    Supplier<List<TestDto>> source,
    int pageSize,
    CsvRowErrorCollector errorCollector
  ) {
    return new BufferedPageSupplier<>(
      new CsvValidatedRowSupplier<>(
        source,
        Validation.buildDefaultValidatorFactory().getValidator(),
        errorCollector
      ),
      pageSize
    );
  }

  @SafeVarargs
  private final Supplier<List<TestDto>> batches(List<TestDto>... batches) {
    var batchIterator = List.of(batches).iterator();
    return () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();
  }

  private TestDto valid(String name) {
    return new TestDto(name, name + "@example.com", 1);
  }

  private TestDto invalid() {
    return new TestDto("", "invalid-email", -1);
  }

  private List<String> names(List<TestDto> rows) {
    return rows.stream().map(TestDto::getName).toList();
  }

  private List<String> readErrorRows(CsvRowErrorCollector errorCollector, Path csvFile) throws IOException {
    var errorPath = errorCollector.writeToFile(csvFile);
    if (errorPath.isEmpty()) {
      return List.of();
    }
    return Files.readAllLines(errorPath.get(), StandardCharsets.UTF_8).stream().skip(1).toList();
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
