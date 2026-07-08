package it.gov.pagopa.mypay2pu.extractor.service.export;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A decorator supplier that validates each CSV row during batch retrieval.
 * Wraps a batch supplier and applies Jakarta Validation constraints to each row.
 * Invalid rows are filtered out and their errors collected; only valid rows are returned to the CSV writer.
 * The source supplier is expected to return batches of rows and signal end-of-data with empty/null lists.
 * Row numbers start at 2 (CSV header = row 1) and are tracked across all batches and multiple get() calls.
 * Errors are collected in CsvRowErrorCollector for later reporting to a separate .errors.csv file.
 *
 * @param <T> the type of row objects being validated (typically a DTO with Jakarta Validation annotations)
 * @see CsvRowErrorCollector
 */
public class CsvRowValidationSupplier<T> implements Supplier<List<T>> {

  private static final long FIRST_DATA_ROW_NUMBER = 2L;

  private final Supplier<List<T>> source;
  private final Validator validator;
  private final CsvRowErrorCollector errorCollector;
  private final AtomicLong rowNumber = new AtomicLong(FIRST_DATA_ROW_NUMBER);

  /**
   * Constructs a CSV row validation supplier that wraps the source supplier with validation logic.
   * The source supplier must be a batch supplier: each call to supplier.get() returns a batch of rows.
   * When the source supplier returns an empty list or null, it signals that no more data is available.
   * The supplier will be called repeatedly across multiple get() invocations until exhausted.
   *
   * @param source the batch supplier that provides rows to validate; must return empty/null when exhausted
   * @param validator the Jakarta Validator instance to validate rows
   * @param errorCollector the collector to accumulate validation errors
   */
  public CsvRowValidationSupplier(Supplier<List<T>> source, Validator validator, CsvRowErrorCollector errorCollector) {
    this.source = source;
    this.validator = validator;
    this.errorCollector = errorCollector;
  }

  /**
   * Fetches and validates the next batch of rows, returning only valid rows.
   * Behavior: fetches batches from the source supplier and validates each row.
   * Returns the first batch that contains at least one valid row.
   * If a batch contains only invalid rows, continues fetching from the source supplier until valid rows are found or source is exhausted.
   * Each call to this method may invoke the source supplier multiple times internally.
   * Calling this method repeatedly will eventually exhaust the source supplier and return empty lists.
   *
   * @return a list of valid rows from the next available batch, or empty list if no more valid data
   */
  @Override
  public List<T> get() {
    List<T> rows = source.get();
    while (rows != null && !rows.isEmpty()) {
      List<T> validRows = new ArrayList<>(rows.size());
      for (T row : rows) {
        if (validateAndCollectErrors(row)) {
          validRows.add(row);
        }
      }
      if (!validRows.isEmpty()) {
        return validRows;
      }
      rows = source.get();
    }
    return List.of();
  }

  /**
   * Validates a single row and collects any constraint violations.
   * Row number is incremented for every row to ensure accurate error reporting in the CSV error report.
   *
   * @param row the row to validate
   * @return true if the row is valid, false otherwise
   */
  private boolean validateAndCollectErrors(T row) {
    long currentRowNumber = rowNumber.getAndIncrement();
    Set<ConstraintViolation<T>> violations = validator.validate(row);
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
