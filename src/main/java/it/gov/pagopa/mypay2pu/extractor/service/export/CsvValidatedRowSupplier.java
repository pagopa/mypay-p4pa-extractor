package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A decorator supplier that maps source rows to CSV DTOs, validates them, and filters invalid rows.
 * Recoverable mapping failures are collected in the CSV error report while unexpected failures are
 * propagated to stop the export.
 *
 * @param <S> source row type, typically a DB model
 * @param <C> mapped CSV DTO type
 * @see CsvRowErrorCollector
 */
public class CsvValidatedRowSupplier<S, C extends CsvExportDto> implements Supplier<List<C>> {

  private static final long FIRST_DATA_ROW_NUMBER = 2L;
  private static final String MAPPING_ERROR_CODE = "EnumMapping";

  private final Supplier<List<S>> source;
  private final Function<S, C> mapper;
  private final Validator validator;
  private final CsvRowErrorCollector errorCollector;
  private final AtomicLong rowNumber = new AtomicLong(FIRST_DATA_ROW_NUMBER);

  /**
   * Constructs a CSV row validation supplier for already-mapped CSV DTOs.
   *
   * @param source the batch supplier that provides CSV DTO rows
   * @param validator the Jakarta Validator instance to validate rows
   * @param errorCollector the collector to accumulate row errors
   */
  @SuppressWarnings("unchecked")
  public CsvValidatedRowSupplier(
    Supplier<List<C>> source,
    Validator validator,
    CsvRowErrorCollector errorCollector
  ) {
    // This overload is safe because its source and target types are both C.
    this.source = (Supplier<List<S>>) (Supplier<?>) Objects.requireNonNull(source, "Source supplier is required");
    this.mapper = sourceRow -> (C) sourceRow;
    this.validator = Objects.requireNonNull(validator, "Validator is required");
    this.errorCollector = Objects.requireNonNull(errorCollector, "Error collector is required");
  }

  /**
   * Constructs a CSV row mapping and validation supplier.
   *
   * @param source the batch supplier that provides source rows, typically DB models
   * @param mapper maps a source row to a CSV DTO; recoverable mapping failures must throw
   *               {@link CsvRowMappingException}
   * @param validator the Jakarta Validator instance to validate mapped rows
   * @param errorCollector the collector to accumulate mapping and validation errors
   */
  public CsvValidatedRowSupplier(
    Supplier<List<S>> source,
    Function<S, C> mapper,
    Validator validator,
    CsvRowErrorCollector errorCollector
  ) {
    this.source = Objects.requireNonNull(source, "Source supplier is required");
    this.mapper = Objects.requireNonNull(mapper, "Mapper is required");
    this.validator = Objects.requireNonNull(validator, "Validator is required");
    this.errorCollector = Objects.requireNonNull(errorCollector, "Error collector is required");
  }

  /**
   * Fetches, maps and validates source batches, returning only valid CSV rows. If a batch contains
   * only rejected rows, the next batch is fetched until a valid row is found or the source ends.
   */
  @Override
  public List<C> get() {
    while (true) {
      List<S> sourceRows = source.get();
      if (CollectionUtils.isEmpty(sourceRows)) {
        return List.of();
      }

      List<C> validRows = new ArrayList<>();
      for (S sourceRow : sourceRows) {
        long currentRowNumber = rowNumber.getAndIncrement();
        C csvRow = mapAndCollectErrors(sourceRow, currentRowNumber);
        if (csvRow != null && validateAndCollectErrors(csvRow, currentRowNumber)) {
          validRows.add(csvRow);
        }
      }
      if (!validRows.isEmpty()) {
        return validRows;
      }
    }
  }

  private C mapAndCollectErrors(S sourceRow, long currentRowNumber) {
    try {
      return mapper.apply(sourceRow);
    } catch (CsvRowMappingException e) {
      errorCollector.add(
        currentRowNumber,
        e.getField(),
        MAPPING_ERROR_CODE,
        e.getMessage(),
        e.getRejectedValue()
      );
      return null;
    }
  }

  private boolean validateAndCollectErrors(C row, long currentRowNumber) {
    Set<ConstraintViolation<C>> violations = validator.validate(row);
    if (violations.isEmpty()) {
      return true;
    }
    violations.stream()
      .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
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