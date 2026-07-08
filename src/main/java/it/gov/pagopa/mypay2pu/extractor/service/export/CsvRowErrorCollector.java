package it.gov.pagopa.mypay2pu.extractor.service.export;

import com.opencsv.ICSVWriter;
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
public class CsvRowErrorCollector implements AutoCloseable {

  private static final String[] ERROR_REPORT_HEADER_ROW =
    {"rowNumber", "field", "code", "message", "rejectedValue"};
  private static final List<String[]> ERROR_REPORT_HEADER = java.util.Collections.singletonList(ERROR_REPORT_HEADER_ROW);

  private final CsvService csvService;
  private final Path csvFilePath;
  private final List<ValidationError> errors = new ArrayList<>();
  private ICSVWriter incrementalWriter;
  private long errorCount;

  /**
   * Constructs a CSV row error collector.
   *
   * @param csvService the CSV service used to write the error report file
   */
  public CsvRowErrorCollector(CsvService csvService) {
    this(csvService, null);
  }

  public CsvRowErrorCollector(CsvService csvService, Path csvFilePath) {
    this.csvService = csvService;
    this.csvFilePath = csvFilePath;
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
    ValidationError validationError = new ValidationError(rowNumber, field, code, message, rejectedValue);
    errorCount++;
    if (csvFilePath == null) {
      errors.add(validationError);
      return;
    }
    writeIncrementalError(validationError);
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
    if (this.csvFilePath != null) {
      closeIncrementalWriter();
      return errorCount == 0 ? Optional.empty() : Optional.of(buildErrorReportPath(this.csvFilePath));
    }
    if (errors.isEmpty()) {
      return Optional.empty();
    }
    Path errorReportPath = buildErrorReportPath(csvFilePath);
    csvService.createCsv(
      errorReportPath,
      ERROR_REPORT_HEADER,
      errors.stream().map(this::toCsvErrorRow).toList()
    );
    return Optional.of(errorReportPath);
  }

  @Override
  public void close() {
    try {
      closeIncrementalWriter();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot close CSV error report writer", e);
    }
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

  private void writeIncrementalError(ValidationError error) {
    ensureIncrementalWriter();
    incrementalWriter.writeNext(toCsvErrorRow(error));
  }

  private void ensureIncrementalWriter() {
    if (incrementalWriter != null) {
      return;
    }
    Path incrementalErrorReportPath = buildErrorReportPath(csvFilePath);
    try {
      incrementalWriter = csvService.openCsvWriter(incrementalErrorReportPath, false);
      incrementalWriter.writeAll(ERROR_REPORT_HEADER);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot open CSV error report file: " + incrementalErrorReportPath, e);
    }
  }

  private void closeIncrementalWriter() throws IOException {
    if (incrementalWriter != null) {
      incrementalWriter.close();
      incrementalWriter = null;
    }
  }

  private String[] toCsvErrorRow(ValidationError error) {
    return new String[]{
      String.valueOf(error.rowNumber()),
      error.field(),
      error.code(),
      error.message(),
      error.rejectedValue()
    };
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
