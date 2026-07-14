package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A decorator supplier that validates CSV DTO batches and filters invalid rows.
 *
 * @param <C> the type of CSV DTO being validated
 * @see CsvRowErrorCollector
 */
public class CsvValidatedRowSupplier<C extends CsvExportDto> implements Supplier<List<C>> {

  private static final long FIRST_DATA_ROW_NUMBER = 2L;

  private final Supplier<List<C>> source;
  private final Validator validator;
  private final CsvRowErrorCollector errorCollector;
  private final AtomicLong rowNumber = new AtomicLong(FIRST_DATA_ROW_NUMBER);

  /**
   * Constructs a CSV row validation supplier.
   *
   * @param source the batch supplier that provides rows to validate
   * @param validator the Jakarta Validator instance to validate rows
   * @param errorCollector the collector to accumulate validation errors
   */
  public CsvValidatedRowSupplier(
    Supplier<List<C>> source,
    Validator validator,
    CsvRowErrorCollector errorCollector
  ) {
    this.source = Objects.requireNonNull(source, "Source supplier is required");
    this.validator = Objects.requireNonNull(validator, "Validator is required");
    this.errorCollector = Objects.requireNonNull(errorCollector, "Error collector is required");
  }

  /**
   * Fetches source batches until it finds valid rows or the source is exhausted.
   *
   * @return the valid rows from the next source batch containing valid rows, or the source result when exhausted
   */
  @Override
  public List<C> get() {
    while (true) {
      List<C> rows = source.get();
      if (rows == null || rows.isEmpty()) {
        return rows;
      }
      List<C> validRows = rows.stream().filter(this::validateAndCollectErrors).toList();
      if (!validRows.isEmpty()) {
        return validRows;
      }
    }
  }

  /**
   * Validates a single row and collects any constraint violations.
   * Row number is incremented for every row to ensure accurate error reporting in the CSV error report.
   *
   * @param row the CSV DTO to validate
   * @return true if the row is valid, false otherwise
   */
  private boolean validateAndCollectErrors(C row) {
    long currentRowNumber = rowNumber.getAndIncrement();
    Set<ConstraintViolation<C>> violations = validator.validate(row);
    if (violations.isEmpty()) {
      return true;
    }
    violations.stream()
      .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
      .forEach(violation -> {
        Object rejectedValue = violation.getInvalidValue();
        errorCollector.add(
          currentRowNumber,
          violation.getPropertyPath().toString(),
          violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
          violation.getMessage(),
          rejectedValue != null ? rejectedValue.toString() : ""
        );
      });
    return false;
  }
}
