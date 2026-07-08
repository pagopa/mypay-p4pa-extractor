package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Collects and manages validation errors for CSV rows.
 * Stores constraint violations from Jakarta Validation and can write them to a separate error report file.
 * Each error records the row number, field name, violation code, message, and rejected value.
 * The error report file is named as {original_filename}.errors.csv and placed in the same directory.
 *
 * @see CsvRowValidationSupplier
 */
public class CsvRowErrorCollector {

  private static final String[] ERROR_REPORT_HEADER_ROW =
    {"rowNumber", "field", "code", "message", "rejectedValue"};
  private static final List<String[]> ERROR_REPORT_HEADER = java.util.Collections.singletonList(ERROR_REPORT_HEADER_ROW);

  private final CsvService csvService;
  private final List<ValidationError> errors = new ArrayList<>();

  /**
   * Constructs a CSV row error collector.
   *
   * @param csvService the CSV service used to write the error report file
   */
  public CsvRowErrorCollector(CsvService csvService) {
    this.csvService = csvService;
  }

  /**
   * Adds a validation error for a row.
   *
   * @param rowNumber the 1-based row number in the CSV file
   * @param field the field name that failed validation
   * @param code the constraint annotation class name (e.g., NotBlank, Email, etc.)
   * @param message the validation error message
   * @param rejectedValue the value that failed validation
   */
  public void add(long rowNumber, String field, String code, String message, String rejectedValue) {
    errors.add(new ValidationError(rowNumber, field, code, message, rejectedValue));
  }

  /**
   * Returns an immutable copy of all collected errors.
   *
   * @return a list of validation errors, or empty list if no errors
   */
  public List<ValidationError> getErrors() {
    return List.copyOf(errors);
  }

  /**
   * Writes collected errors to a CSV file if any errors exist.
   * File path is derived from the original CSV path: {filename}.csv becomes {filename}.errors.csv.
   * Does not write any file if there are no errors.
   *
   * @param csvFilePath the path of the original CSV file
   * @return an Optional containing the path to the error report file, or empty if no errors to write
   * @throws IOException if writing the error report file fails
   */
  public Optional<Path> writeToFile(Path csvFilePath) throws IOException {
    if (errors.isEmpty()) {
      return Optional.empty();
    }
    Path errorReportPath = buildErrorReportPath(csvFilePath);
    csvService.createCsv(
      errorReportPath,
      ERROR_REPORT_HEADER,
      errors.stream()
        .map(error -> new String[]{
          String.valueOf(error.rowNumber()),
          error.field(),
          error.code(),
          error.message(),
          error.rejectedValue()
        })
        .toList()
    );
    return Optional.of(errorReportPath);
  }

  /**
   * Builds the error report file path from the original CSV file path.
   * Converts {filename}.csv to {filename}.errors.csv.
   *
   * @param csvFilePath the path of the original CSV file
   * @return the path where the error report should be written
   */
  private Path buildErrorReportPath(Path csvFilePath) {
    String csvName = csvFilePath.getFileName().toString();
    String errorReportFileName = csvName.endsWith(".csv") ? csvName.replace(".csv", ".errors.csv") : csvName + ".errors.csv";
    return csvFilePath.resolveSibling(errorReportFileName);
  }

  /**
   * Immutable record representing a single validation error.
   *
   * @param rowNumber the CSV row number (1-based, header is row 1)
   * @param field the field/column name that failed validation
   * @param code the constraint annotation type name
   * @param message the human-readable validation error message
   * @param rejectedValue the invalid value that was rejected
   */
  public record ValidationError(
    long rowNumber,
    String field,
    String code,
    String message,
    String rejectedValue
  ) {
  }
}
