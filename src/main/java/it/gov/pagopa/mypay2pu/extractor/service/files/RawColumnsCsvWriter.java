package it.gov.pagopa.mypay2pu.extractor.service.files;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

class RawColumnsCsvWriter extends CSVWriter {

  private final Set<String> rawColumnNames;
  private Set<Integer> rawColumnIndexes = Set.of();
  private boolean headerProcessed;

  RawColumnsCsvWriter(Writer writer, char separator, char quoteChar, Set<String> rawColumnNames) {
    super(writer, separator, quoteChar, DEFAULT_ESCAPE_CHARACTER, DEFAULT_LINE_END);
    this.rawColumnNames = rawColumnNames;
  }

  @Override
  protected void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException {
    if (nextLine == null) {
      return;
    }
    if (!headerProcessed) {
      rawColumnIndexes = findRawColumnIndexes(nextLine);
      headerProcessed = true;
    }

    for (int columnIndex = 0; columnIndex < nextLine.length; columnIndex++) {
      if (columnIndex > 0) {
        appendable.append(separator);
      }

      String value = nextLine[columnIndex];
      if (value == null) {
        continue;
      }
      if (rawColumnIndexes.contains(columnIndex)) {
        appendable.append(removeLineBreaks(value));
      } else {
        appendCsvValue(value, applyQuotesToAll, appendable);
      }
    }
    appendable.append(getLineEnd());
    getWriter().write(appendable.toString());
  }

  private Set<Integer> findRawColumnIndexes(String[] header) {
    Set<Integer> indexes = new HashSet<>();
    for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
      if (rawColumnNames.contains(header[columnIndex])) {
        indexes.add(columnIndex);
      }
    }
    return indexes;
  }

  private String removeLineBreaks(String value) {
    return value.replace("\r", "").replace("\n", "");
  }

  private void appendCsvValue(String value, boolean applyQuotesToAll, Appendable appendable) throws IOException {
    boolean containsSpecialCharacters = stringContainsSpecialCharacters(value);
    if (applyQuotesToAll || containsSpecialCharacters) {
      appendable.append(quotechar);
    }
    if (containsSpecialCharacters) {
      processLine(value, appendable);
    } else {
      appendable.append(value);
    }
    if (applyQuotesToAll || containsSpecialCharacters) {
      appendable.append(quotechar);
    }
  }
}
