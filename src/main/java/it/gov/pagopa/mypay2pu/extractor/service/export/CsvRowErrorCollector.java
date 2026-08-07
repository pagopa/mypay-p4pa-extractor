package it.gov.pagopa.mypay2pu.extractor.service.export;

import com.opencsv.ICSVWriter;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Collects and manages validation errors for CSV rows.
 * Stores constraint violations from Jakarta Validation and can write them to a separate error report file.
 * Each error records the row number, source logical key, field name, violation code, message, and rejected value.
 * The error report file is named as {@code {original_filename}.errors.csv} and placed in the same directory.
 * It supports two modes:
 * <ul>
 *   <li><b>in-memory collection</b>, when no CSV path is provided in the constructor:
 *       errors are kept in memory and written later by {@link #writeToFile(Path)}</li>
 *   <li><b>incremental write</b>, when a CSV path is provided in the constructor:
 *       any pre-existing error file is deleted on first write, then errors are appended
 *       as they are collected; the CSV header is always written once at file creation</li>
 * </ul>
 *
 * @see CsvValidatedRowSupplier
 */
public class CsvRowErrorCollector implements AutoCloseable {

  private static final String[] ERROR_REPORT_HEADER_ROW =
    {"rowNumber", "logicalKey", "field", "code", "message", "rejectedValue"};
  private static final List<String[]> ERROR_REPORT_HEADER = java.util.Collections.singletonList(ERROR_REPORT_HEADER_ROW);

  private final CsvService csvService;
  private final Path errorsCsvFilePath;
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

  /**
   * Constructs a CSV row error collector.
   *
   * @param csvService the CSV service used to write the error report file
   * @param errorsCsvFilePath the source CSV file path used to derive and store the error report file path
   *                    ({@code source.csv -> source.errors.csv});
   *                    when {@code null}, errors are kept in memory until {@link #writeToFile(Path)} is called;
   *                    when non-null, errors are written incrementally to disk
   */
  public CsvRowErrorCollector(CsvService csvService, Path errorsCsvFilePath) {
    this.csvService = csvService;
    this.errorsCsvFilePath = errorsCsvFilePath != null ? buildErrorReportPath(errorsCsvFilePath) : null;
  }

  /**
   * Adds a validation error for a row.
   *
   * @param rowNumber the 1-based row number in the CSV file
   * @param logicalKey the source row logical key
   * @param field the field name that failed validation
   * @param code the constraint annotation class name (e.g., NotBlank, Email, etc.)
   * @param message the validation error message
   * @param rejectedValue the value that failed validation
   */
  public void add(long rowNumber, String logicalKey, String field, String code, String message, String rejectedValue) {
    ValidationError validationError = new ValidationError(rowNumber, logicalKey, field, code, message, rejectedValue);
    errorCount++;
    if (errorsCsvFilePath == null) {
      errors.add(validationError);
      return;
    }
    writeIncrementalError(validationError);
  }

  /**
   * Writes collected errors to a CSV file if any errors exist.
   *
   * <p>In incremental mode (constructor received a non-null source CSV path), this method closes the writer and ignores the
   * method parameter path, then returns the incremental error file path when at least one error was collected.
   * If no errors were collected, it removes any pre-existing error file and returns empty.
   * In in-memory mode, it writes all collected errors using the method parameter path.</p>
   *
   * <p>File path is derived as {@code {filename}.csv -> {filename}.errors.csv}. No file is written when there are
   * no errors.</p>
   *
   * @param csvFilePath the path of the original CSV file, used only in in-memory mode
   * @return an Optional containing the path to the error report file, or empty if no errors to write
   * @throws IOException if writing the error report file fails
   */
  public Optional<Path> writeToFile(Path csvFilePath) throws IOException {
    if (this.errorsCsvFilePath != null) {
      closeIncrementalWriter();
      if (errorCount == 0) {
        Files.deleteIfExists(this.errorsCsvFilePath);
        return Optional.empty();
      }
      return Optional.of(this.errorsCsvFilePath);
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

  /**
   * Closes the incremental writer when present.
   *
   * @throws IllegalStateException if the writer cannot be closed
   */
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
   * Converts {@code {filename}.csv} to {@code {filename}.errors.csv}.
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
    try {
      Files.deleteIfExists(errorsCsvFilePath);
      incrementalWriter = csvService.openCsvWriter(errorsCsvFilePath, true);
      incrementalWriter.writeAll(ERROR_REPORT_HEADER);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot open CSV error report file: " + errorsCsvFilePath, e);
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
      error.logicalKey(),
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
   * @param logicalKey the source row logical key
   * @param field the field/column name that failed validation
   * @param code the constraint annotation type name
   * @param message the human-readable validation error message
   * @param rejectedValue the invalid value that was rejected
   */
  public record ValidationError(
    long rowNumber,
    String logicalKey,
    String field,
    String code,
    String message,
    String rejectedValue
  ) {
  }
}
