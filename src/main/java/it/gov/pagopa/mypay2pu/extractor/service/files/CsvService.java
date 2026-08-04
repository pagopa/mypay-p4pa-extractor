package it.gov.pagopa.mypay2pu.extractor.service.files;


import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
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
import java.util.Set;
import java.util.function.Supplier;

/**
 * Service responsible for creating CSV files using OpenCSV.
 * Supports writing raw string rows or bean-based rows through a supplier.
 */
@Lazy
@Service
@Slf4j
public class CsvService {

  private final char separator;
  private final char quoteChar;

  /**
   * Constructs the CSV service with configured CSV formatting characters.
   *
   * @param separator the configured field separator character
   * @param quoteChar the configured quote character
   */
  public CsvService(
    @Value("${csv.separator}") char separator,
    @Value("${csv.quote-char}") char quoteChar) {
    this.separator = separator;
    this.quoteChar = quoteChar;
  }

  /**
   * Creates a CSV file from the provided header and data.
   *
   * @param csvFilePath the full path where the CSV file should be saved
   * @param header the CSV header rows
   * @param data the CSV data rows
   * @throws IOException if an error occurs while writing the file
   */
  public void createCsv(Path csvFilePath, List<String[]> header, List<String[]> data) throws IOException {
    try (ICSVWriter csvWriter = openCsvWriter(csvFilePath, false)) {
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
   * Creates a CSV file from a supplier of bean batches.
   *
   * <p>The supplier is called repeatedly until it returns {@code null} or an empty list.</p>
   *
   * <p>Bean serialization is delegated to {@link StatefulBeanToCsv}, configured with the given profile and
   * an {@link OrderedHeaderColumnNameMappingStrategy}.</p>
   *
   * @param <C> the generic type of the beans to be written to the CSV
   * @param csvFilePath the path to the CSV file to write
   * @param typeClass the class type of the beans to be written to the CSV
   * @param csvRowsSupplier a supplier of beans to be written to the CSV (called multiple times until data are returned)
   * @param csvProfile the profile to be used for writing the CSV
   * @throws IOException if an error occurs while writing the file
   */
  public <C extends CsvExportDto> void createCsv(
    Path csvFilePath,
    Class<C> typeClass,
    Supplier<List<C>> csvRowsSupplier,
    String csvProfile
  ) throws IOException {

    ensureParentDirectory(csvFilePath.toFile());

    try (Writer writer = Files.newBufferedWriter(csvFilePath)) {
      OrderedHeaderColumnNameMappingStrategy<C> mappingStrategy = new OrderedHeaderColumnNameMappingStrategy<>();
      mappingStrategy.setType(typeClass);
      mappingStrategy.setProfile(csvProfile);

      StatefulBeanToCsv<C> beanToCsv = new StatefulBeanToCsvBuilder<C>(buildBeanWriter(writer, typeClass))
        .withProfile(csvProfile)
        .withSeparator(separator)
        .withQuotechar(quoteChar)
        .withApplyQuotesToAll(false)
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

  private <C extends CsvExportDto> ICSVWriter buildBeanWriter(Writer writer, Class<C> typeClass) {
    CsvRawColumns csvRawColumns = typeClass.getAnnotation(CsvRawColumns.class);
    if (csvRawColumns == null) {
      return new CSVWriterBuilder(writer)
        .withSeparator(separator)
        .withQuoteChar(quoteChar)
        .build();
    }
    return new RawColumnsCsvWriter(writer, separator, quoteChar, Set.of(csvRawColumns.value()));
  }

  /**
   * Opens an {@link ICSVWriter} for the given CSV path.
   *
   * @param csvFilePath the target CSV file path
   * @param append whether to append to an existing file ({@code true}) or overwrite ({@code false})
   * @return an initialized CSV writer
   * @throws IOException if the parent directory cannot be created or the file cannot be opened
   */
  public ICSVWriter openCsvWriter(Path csvFilePath, boolean append) throws IOException {
    File file = csvFilePath.toFile();
    ensureParentDirectory(file);
    return buildCsvWriter(file, append);
  }

  private void ensureParentDirectory(File file) throws IOException {
    File parentDir = file.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
    }
  }

  private ICSVWriter buildCsvWriter(File file, boolean append) throws IOException {
    return new CSVWriterBuilder(new FileWriter(file, append))
      .withSeparator(separator)
      .withQuoteChar(quoteChar)
      .build();
  }
}
