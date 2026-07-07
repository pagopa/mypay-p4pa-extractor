package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationErrorCollector {

  private static final String[] ERROR_REPORT_HEADER_ROW =
    {"rowNumber", "field", "code", "message", "rejectedValue"};
  private static final List<String[]> ERROR_REPORT_HEADER = java.util.Collections.singletonList(ERROR_REPORT_HEADER_ROW);

  private final CsvService csvService;
  private final List<ValidationError> errors = new ArrayList<>();

  public ValidationErrorCollector(CsvService csvService) {
    this.csvService = csvService;
  }

  public void add(long rowNumber, String field, String code, String message, String rejectedValue) {
    errors.add(new ValidationError(rowNumber, field, code, message, rejectedValue));
  }

  public List<ValidationError> getErrors() {
    return List.copyOf(errors);
  }

  public void writeToFile(Path csvFilePath) throws IOException {
    if (errors.isEmpty()) {
      return;
    }
    csvService.createCsv(
      buildErrorReportPath(csvFilePath),
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
  }

  private Path buildErrorReportPath(Path csvFilePath) {
    String csvName = csvFilePath.getFileName().toString();
    String errorReportFileName = csvName.endsWith(".csv") ? csvName.replace(".csv", ".errors.csv") : csvName + ".errors.csv";
    return csvFilePath.resolveSibling(errorReportFileName);
  }

  public record ValidationError(
    long rowNumber,
    String field,
    String code,
    String message,
    String rejectedValue
  ) {
  }
}
