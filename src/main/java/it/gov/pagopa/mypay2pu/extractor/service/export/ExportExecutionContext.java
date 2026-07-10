package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;

import java.nio.file.Path;

/**
 * Immutable execution context for a single export run.
 * It groups together the resolved configuration and runtime metadata needed
 * across batching, file generation, and archiving steps.
 *
 * @param extractionDirectory the working directory for the current extraction
 * @param migrationFileType the exported migration file type
 * @param brokerCf the broker fiscal code used in generated file names and rows
 * @param zipVersion the configured export version used in file naming and CSV profile selection
 * @param extractionTimestamp the timestamp associated with the current export execution
 * @param maxRowsPerPart the maximum number of rows allowed in each generated part
 * @param pageSize the page size to use when consuming paged data sources
 */
public record ExportExecutionContext(
  Path extractionDirectory,
  MigrationFileType migrationFileType,
  String brokerCf,
  String zipVersion,
  String extractionTimestamp,
  int maxRowsPerPart,
  int pageSize
) {

  /**
   * Builds the base file name for the current export part.
   * When the part number is {@code null}, the export is treated as a single-file export.
   *
   * @param part the part number for multipart exports, or {@code null} for single-file exports
   * @return the base file name without extension
   */
  public String buildExportBaseFileName(Integer part) {
    return part != null
      ? "%s-%s-%s-part%d-%s".formatted(
          brokerCf,
          migrationFileType.name(),
          extractionTimestamp,
          part,
          zipVersion
        )
      : "%s-%s-%s-%s".formatted(
          brokerCf,
          migrationFileType.name(),
          extractionTimestamp,
          zipVersion
        );
  }

  /**
   * Resolves the CSV path for the given export file base name.
   *
   * @param exportBaseFileName the export base file name without extension
   * @return the absolute path of the CSV file to create
   */
  public Path resolveCsvPath(String exportBaseFileName) {
    return extractionDirectory.resolve(exportBaseFileName + ".csv");
  }

  /**
   * Resolves the ZIP path for the given export file base name.
   *
   * @param exportBaseFileName the export base file name without extension
   * @return the absolute path of the ZIP file to create
   */
  public Path resolveZipPath(String exportBaseFileName) {
    return extractionDirectory.resolve(exportBaseFileName + ".zip");
  }
}
