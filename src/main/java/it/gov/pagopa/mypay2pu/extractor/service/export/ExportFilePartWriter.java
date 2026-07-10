package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Writes a single export part to CSV, collects validation errors, and archives the result as ZIP.
 * This component encapsulates file-system side effects so that export services can focus on orchestration.
 */
@Component
public class ExportFilePartWriter {

  private final CsvService csvService;
  private final Validator validator;
  private final FileArchiverService fileArchiverService;

  public ExportFilePartWriter(
    CsvService csvService,
    Validator validator,
    FileArchiverService fileArchiverService
  ) {
    this.csvService = csvService;
    this.validator = validator;
    this.fileArchiverService = fileArchiverService;
  }

  /**
   * Writes one export part using the provided DTO supplier and archives the generated CSV as ZIP.
   *
   * @param executionContext the immutable execution context for the current export
   * @param exportBaseFileName the base file name without extension
   * @param dtoClass the DTO class used by the CSV writer
   * @param csvRowsSupplier the supplier returning CSV rows in batches
   * @param <D> the type of DTO written to the CSV
   * @return the result describing the generated ZIP file and optional error file
   * @throws IllegalStateException if CSV creation or archiving fails
   */
  public <D> ExportPartResult writePart(
    ExportExecutionContext executionContext,
    String exportBaseFileName,
    Class<D> dtoClass,
    Supplier<List<D>> csvRowsSupplier
  ) {
    Path csvPath = executionContext.resolveCsvPath(exportBaseFileName);
    Path zipPath = executionContext.resolveZipPath(exportBaseFileName);
    Optional<String> errorFileName;

    try (CsvRowErrorCollector errorCollector = new CsvRowErrorCollector(csvService, csvPath)) {
      CsvValidatedRowSupplier<D> validatingSupplier =
        new CsvValidatedRowSupplier<>(csvRowsSupplier, validator, errorCollector);
      csvService.createCsv(csvPath, dtoClass, validatingSupplier, executionContext.zipVersion());
      errorFileName = errorCollector.writeToFile(csvPath)
        .map(Path::getFileName)
        .map(Path::toString);
    } catch (IOException e) {
      throw new IllegalStateException("Error writing to CSV file: " + e.getMessage(), e);
    }

    compressAndArchive(csvPath, zipPath);
    return new ExportPartResult(zipPath.getFileName().toString(), errorFileName);
  }

  private void compressAndArchive(Path csvPath, Path zipPath) {
    try {
      fileArchiverService.zipFile(csvPath, zipPath);
      Files.deleteIfExists(csvPath);
    } catch (IOException e) {
      throw new IllegalStateException(
        "Cannot archive files: " + csvPath + " into destination: " + zipPath,
        e
      );
    }
  }
}
