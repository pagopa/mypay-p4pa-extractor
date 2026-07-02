package it.gov.pagopa.mypay2pu.extractor.service.files;


import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.mypay2pu.extractor.exception.InvalidCsvRowException;
import it.gov.pagopa.mypay2pu.extractor.service.files.xls.OrderedHeaderColumnNameMappingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

@Lazy
@Service
@Slf4j
public class CsvService {

  private final char separator;
  private final char quoteChar;

  public CsvService(
    @Value("${csv.separator}") char separator,
    @Value("${csv.quote-char}") char quoteChar) {
    this.separator = separator;
    this.quoteChar = quoteChar;
  }

  /**
   * Creates a CSV file from the provided header and data.
   *
   * @param csvFilePath The full path where the CSV file should be saved.
   * @param header      The header of the CSV, as a list of String[].
   * @param data        The data to write to the CSV, as a list of String[].
   * @throws IOException If an error occurs while writing the file.
   */
  public void createCsv(Path csvFilePath, List<String[]> header, List<String[]> data) throws IOException {
    // Create the destination folder if it doesn't already exist
    File file = csvFilePath.toFile();
    File parentDir = file.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
    }

    // Create the CSV file
    try (ICSVWriter csvWriter = buildCsvWriter(file)) {
      // Write the header
      if (header != null && !header.isEmpty()) {
        csvWriter.writeAll(header);
      }

      // Write the data
      if (data != null && !data.isEmpty()) {
        csvWriter.writeAll(data);
      }
    }
    log.info("CSV file created successfully: {}", csvFilePath);
  }

  /**
   * Creates a CSV file from the provided supplier of beans.
   *
   * <p>This method ensures that the file is properly closed after processing
   * by using a try-with-resources statement.</p>
   *
   * <p>This method uses {@code StatefulBeanToCsv.write()}
   * to write each bean individually, reducing memory consumption at the cost of lower performance.</p>
   *
   * <p>The supplier is called repeatedly until it returns an empty list or null,
   * indicating that there are no more beans to write.</p>
   *
   * @param <C> the generic type of the beans to be written to the CSV
   * @param csvFilePath the path to the CSV file to write
   * @param typeClass the class type of the beans to be written to the CSV
   * @param csvRowsSupplier a supplier of beans to be written to the CSV (called multiple times until data are returned)
   * @param csvProfile the profile to be used for writing the CSV
   * @throws IOException if an error occurs while writing the file
   */
  public <C> void createCsv(Path csvFilePath, Class<C> typeClass, Supplier<List<C>> csvRowsSupplier, String csvProfile) throws IOException {

    File file = csvFilePath.toFile();
    File parentDir = file.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
    }

    try (Writer writer = Files.newBufferedWriter(csvFilePath)) {
      OrderedHeaderColumnNameMappingStrategy<C> mappingStrategy = new OrderedHeaderColumnNameMappingStrategy<>();
      mappingStrategy.setType(typeClass);
      mappingStrategy.setProfile(csvProfile);

      StatefulBeanToCsv<C> beanToCsv = new StatefulBeanToCsvBuilder<C>(writer)
        .withProfile(csvProfile)
        .withSeparator(separator)
        .withQuotechar(quoteChar)
        .withMappingStrategy(mappingStrategy)
        .withThrowExceptions(true)
        .build();

      List<C> rows;
      while (!CollectionUtils.isEmpty(rows = csvRowsSupplier.get())) {
        beanToCsv.write(rows);
      }

    } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
      throw new InvalidCsvRowException("Invalid CSV row: " + e.getMessage());
    }
  }

  private ICSVWriter buildCsvWriter(File file) throws IOException {
    return new CSVWriterBuilder(new FileWriter(file))
      .withSeparator(separator)
      .withQuoteChar(quoteChar)
      .build();
  }
}
