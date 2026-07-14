package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.ExportExecutionContext;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportPartResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Writes one export part, including the CSV, optional error report, and ZIP archive.
 */
@Component
public class ExportFilePartWriter {

  private final CsvService csvService;
  private final FileArchiverService fileArchiverService;

  public ExportFilePartWriter(CsvService csvService, FileArchiverService fileArchiverService) {
    this.csvService = csvService;
    this.fileArchiverService = fileArchiverService;
  }

  /**
   * Creates the collector used to persist CSV validation errors for the current part.
   *
   * @param executionContext the export context with resolved paths
   * @param exportBaseFileName the base file name for the current export part
   * @return a new error collector bound to the CSV path
   */
  public CsvRowErrorCollector createErrorCollector(
    ExportExecutionContext executionContext,
    String exportBaseFileName
  ) {
    return new CsvRowErrorCollector(csvService, executionContext.resolveCsvPath(exportBaseFileName));
  }

  /**
   * Writes the CSV part, optionally persists validation errors, and archives the result into a ZIP file.
   *
   * @param executionContext the export context with resolved paths and ZIP version
   * @param exportBaseFileName the base file name for the current export part
   * @param dtoClass the exported CSV DTO type
   * @param csvRowsSupplier the supplier that provides rows to write
   * @param errorCollector the error collector used to persist validation errors
   * @param <C> the exported DTO type
   * @return the generated part result with ZIP file name and optional error file name
   */
  public <C extends CsvExportDto> ExportPartResult writePart(
    ExportExecutionContext executionContext,
    String exportBaseFileName,
    Class<C> dtoClass,
    Supplier<List<C>> csvRowsSupplier,
    CsvRowErrorCollector errorCollector
  ) {
    Path csvPath = executionContext.resolveCsvPath(exportBaseFileName);
    Path zipPath = executionContext.resolveZipPath(exportBaseFileName);
    Optional<String> errorFileName;

    try (errorCollector) {
      csvService.createCsv(csvPath, dtoClass, csvRowsSupplier, executionContext.zipVersion());
      errorFileName = errorCollector.writeToFile(csvPath)
        .map(Path::getFileName)
        .map(Path::toString);
    } catch (IOException e) {
      throw new IllegalStateException("Error writing to CSV file: " + e.getMessage(), e);
    }

    try {
      fileArchiverService.zipFile(csvPath, zipPath);
      Files.deleteIfExists(csvPath);
    } catch (IOException e) {
      throw new IllegalStateException(
        "Cannot archive files: " + csvPath + " into destination: " + zipPath,
        e
      );
    }

    return new ExportPartResult(zipPath.getFileName().toString(), errorFileName);
  }
}
