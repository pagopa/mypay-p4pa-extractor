package it.gov.pagopa.mypay2pu.extractor.service.export;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class ValidatingCsvSupplier<T> implements Supplier<List<T>> {

  private static final long FIRST_DATA_ROW_NUMBER = 2L;

  private final Supplier<List<T>> source;
  private final Validator validator;
  private final ValidationErrorCollector errorCollector;
  private final AtomicLong rowNumber = new AtomicLong(FIRST_DATA_ROW_NUMBER);

  public ValidatingCsvSupplier(Supplier<List<T>> source, Validator validator, ValidationErrorCollector errorCollector) {
    this.source = source;
    this.validator = validator;
    this.errorCollector = errorCollector;
  }

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

