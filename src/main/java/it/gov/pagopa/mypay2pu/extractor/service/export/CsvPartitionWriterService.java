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
   * @param csvFilePath base CSV file path
   * @param typeClass DTO class used for CSV serialization
   * @param sourceSupplier supplier that provides validated export rows
   * @param csvProfile CSV profile used by the serializer
   * @param maxRowsPerPart maximum number of rows per CSV part
   * @param <C> CSV DTO type
   * @return generated CSV paths in deterministic order
   * @throws IOException if file creation or renaming fails
   */
  public <C extends CsvExportDto> List<Path> writeCsv(Path csvFilePath,
                                                      Class<C> typeClass,
                                                      Supplier<List<C>> sourceSupplier,
                                                      String csvProfile,
                                                      int maxRowsPerPart) throws IOException {
    if (maxRowsPerPart <= 0) {
      throw new IllegalArgumentException("Max rows per part must be positive");
    }

    Supplier<List<C>> partitionedRowsSupplier = new BufferedPageSupplier<>(sourceSupplier, maxRowsPerPart);
    List<Path> partPaths = new ArrayList<>();
    int partNumber = 1;
    List<C> partRows;
    while (!(partRows = partitionedRowsSupplier.get()).isEmpty()) {
      Path partPath = buildPartPath(csvFilePath, partNumber++);
      List<C> currentPartRows = partRows;
      AtomicBoolean delivered = new AtomicBoolean(false);
      csvService.createCsv(
        partPath,
        typeClass,
        () -> delivered.compareAndSet(false, true) ? currentPartRows : List.of(),
        csvProfile
      );
      partPaths.add(partPath);
    }

    if (partPaths.isEmpty()) {
      csvService.createCsv(csvFilePath, typeClass, List::of, csvProfile);
      return List.of(csvFilePath);
    }

    if (partPaths.size() == 1) {
      Path singlePartPath = partPaths.get(0);
      Files.move(singlePartPath, csvFilePath, StandardCopyOption.REPLACE_EXISTING);
      return List.of(csvFilePath);
    }
    return partPaths;
  }

  /**
   * Builds the file path for a numbered CSV part.
   *
   * @param csvFilePath base CSV file path
   * @param partNumber 1-based part number
   * @return file path for the part
   */
  private Path buildPartPath(Path csvFilePath, int partNumber) {
    String fileName = csvFilePath.getFileName().toString();
    String partSuffix = "_part_%03d".formatted(partNumber);
    String partFileName = fileName.endsWith(".csv")
      ? fileName.replace(".csv", partSuffix + ".csv")
      : fileName + partSuffix + ".csv";
    return csvFilePath.resolveSibling(partFileName);
  }
}
