package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Writes CSV exports into one or more deterministic part files.
 *
 * <p>The service keeps the existing CSV serialization flow and only adds
 * partitioning when the configured row threshold is exceeded.</p>
 */
@Service
public class CsvPartitionWriterService {

  private final CsvService csvService;

  public CsvPartitionWriterService(CsvService csvService) {
    this.csvService = csvService;
  }

  /**
   * Writes the CSV output for the given supplier, splitting it into part files
   * when the number of rows exceeds the configured threshold.
   *
   * @param workingDirectory base CSV file path
   * @param fileNameBuilder builder for CSV file names
   * @param typeClass DTO class used for CSV serialization
   * @param sourceSupplier supplier that provides validated export rows
   * @param csvProfile CSV profile used by the serializer
   * @param maxRowsPerPart maximum number of rows per CSV part
   * @return generated CSV paths in deterministic order
   * @throws IOException if file creation or renaming fails
   */
  public <T extends CsvExportDto> List<Path> writeCsv(Path workingDirectory,
                                                      ExportFileNameBuilder fileNameBuilder,
                                                      Class<T> typeClass,
                                                      Supplier<List<T>> sourceSupplier,
                                                      String csvProfile,
                                                      int maxRowsPerPart) throws IOException {
    if (maxRowsPerPart <= 0) {
      throw new IllegalArgumentException("Max rows per part must be positive");
    }

    Supplier<List<T>> partitionedRowsSupplier =
      new BufferedPageSupplier<>(sourceSupplier, maxRowsPerPart);

    List<Path> partPaths = new ArrayList<>();
    int partNumber = 1;

    List<T> partRows;
    while (!(partRows = partitionedRowsSupplier.get()).isEmpty()) {
      Path partPath = workingDirectory.resolve(
        fileNameBuilder.buildCsvPartFileName(partNumber++)
      );

      writePart(partPath, typeClass, partRows, csvProfile);
      partPaths.add(partPath);
    }

    if (partPaths.isEmpty()) {
      Path csvFilePath = workingDirectory.resolve(fileNameBuilder.buildCsvFileName());

      csvService.createCsv(csvFilePath, typeClass, List::of, csvProfile);

      return List.of(csvFilePath);
    }

    if (partPaths.size() == 1) {
      Path singlePartPath = partPaths.getFirst();
      Path csvFilePath = workingDirectory.resolve(
        fileNameBuilder.buildCsvFileName()
      );

      Files.move(singlePartPath, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

      return List.of(csvFilePath);
    }

    return List.copyOf(partPaths);
  }

  /**
   * Writes a single CSV partition containing the provided rows.
   *
   * <p>The rows are supplied to the CSV service only once. Any subsequent
   * invocation of the supplier returns an empty list, signaling that no
   * additional rows are available for the current partition.</p>
   *
   * @param partPath   destination path of the CSV partition
   * @param typeClass  CSV DTO class used for serialization
   * @param rows       rows to write to the partition
   * @param csvProfile CSV serialization profile
   * @param <T>        CSV export DTO type
   * @throws IOException if the CSV partition cannot be created or written
   */
  private <T extends CsvExportDto> void writePart(Path partPath,
                                                  Class<T> typeClass,
                                                  List<T> rows,
                                                  String csvProfile) throws IOException {
    AtomicBoolean delivered = new AtomicBoolean();

    csvService.createCsv(
      partPath,
      typeClass,
      () -> delivered.compareAndSet(false, true)
        ? rows
        : List.of(),
      csvProfile
    );
  }
}
