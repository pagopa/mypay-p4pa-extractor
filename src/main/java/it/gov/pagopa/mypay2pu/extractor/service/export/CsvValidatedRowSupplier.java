package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
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
 * A decorator supplier that maps source-model batches to CSV DTOs, validates them and filters
 * invalid rows.
 *
 * @param <M> source model type
 * @param <C> the type of CSV DTO being validated
 * @see CsvRowErrorCollector
 */
public class CsvValidatedRowSupplier<M extends ExportModel, C extends CsvExportDto> implements Supplier<List<C>> {

  private static final long FIRST_DATA_ROW_NUMBER = 2L;

  private final Supplier<List<M>> source;
  private final Function<M, C> mapper;
  private final Validator validator;
  private final CsvRowErrorCollector errorCollector;
  private final AtomicLong rowNumber = new AtomicLong(FIRST_DATA_ROW_NUMBER);

  /**
   * Constructs a CSV row supplier that maps and validates source batches.
   * The source supplier must be a batch supplier: each call to supplier.get() returns a batch of source models.
   * When the source supplier returns an empty list or null, it signals that no more data is available.
   * The supplier will be called repeatedly across multiple get() invocations until exhausted.
   *
   * @param source the batch supplier that provides source models; must return empty/null when exhausted
   * @param mapper converts each source model to a CSV DTO
   * @param validator the Jakarta Validator instance to validate rows
   * @param errorCollector the collector to accumulate validation errors
   */
  public CsvValidatedRowSupplier(
    Supplier<List<M>> source,
    Function<M, C> mapper,
    Validator validator,
    CsvRowErrorCollector errorCollector
  ) {
    this.source = Objects.requireNonNull(source, "Source supplier is required");
    this.mapper = Objects.requireNonNull(mapper, "Mapper is required");
    this.validator = Objects.requireNonNull(validator, "Validator is required");
    this.errorCollector = Objects.requireNonNull(errorCollector, "Error collector is required");
  }

  /**
   * Fetches, maps and validates the next batch of source models, returning only valid rows.
   * Behavior: fetches batches from the source supplier and maps and validates each row.
   * Returns the first batch that contains at least one valid row.
   * If a batch contains only invalid rows, continues fetching from the source supplier until valid rows are found or source is exhausted.
   * Each call to this method may invoke the source supplier multiple times internally.
   * Calling this method repeatedly will eventually exhaust the source supplier and return empty lists.
   *
   * @return the valid rows from the next source batch containing valid rows, or the source result when exhausted
   */
  @Override
  public List<C> get() {
    while (true) {
      List<M> sourceRows = source.get();
      if (CollectionUtils.isEmpty(sourceRows)) {
        return List.of();
      }
      List<C> validRows = mapAndValidate(sourceRows);
      if (!validRows.isEmpty()) {
        return validRows;
      }
    }
  }

  /**
   * Maps and validates source rows, collecting mapping or validation errors.
   *
   * @param sourceRow the source model to map and validate
   * @return the valid mapped rows
   */
  private List<C> mapAndValidate(List<M> sourceRows) {
    List<C> validRows = new ArrayList<>(sourceRows.size());
    for (M sourceRow : sourceRows) {
      long currentRowNumber = rowNumber.getAndIncrement();
      try {
        C row = mapper.apply(sourceRow);
        if (validateAndCollectErrors(sourceRow, row, currentRowNumber)) {
          validRows.add(row);
        }
      } catch (CsvRowMappingException e) {
        errorCollector.add(
          currentRowNumber,
          sourceRow.logicalKey(),
          e.getField(),
          e.getErrorCode(),
          e.getMessage(),
          e.getRejectedValue()
        );
      } catch (Exception e) {
        errorCollector.add(
          currentRowNumber,
          sourceRow.logicalKey(),
          null,
          "UNEXPECTED ERROR",
          e.getMessage(),
          null
        );
      }
    }
    return validRows;
  }

  private boolean validateAndCollectErrors(M sourceRow, C row, long currentRowNumber) {
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
          sourceRow.logicalKey(),
          violation.getPropertyPath().toString(),
          violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
          violation.getMessage(),
          rejectedValue != null ? rejectedValue.toString() : ""
        );
      });
    return false;
  }
}
