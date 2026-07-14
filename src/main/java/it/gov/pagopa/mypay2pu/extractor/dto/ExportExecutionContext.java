package it.gov.pagopa.mypay2pu.extractor.dto;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;

import java.nio.file.Path;

/**
 * Carries the export parameters and derived paths for a single extraction part.
 *
 * @param extractionDirectory the directory where export artifacts are written
 * @param migrationFileType the migration file type being exported
 * @param brokerIpaCode the broker IPA code used in the export file name
 * @param zipVersion the ZIP version suffix used in the export file name
 * @param extractionTimestamp the extraction timestamp used in the export file name
 * @param rowsToExtract the configured number of rows to extract for the current export
 */
public record ExportExecutionContext(
  Path extractionDirectory,
  MigrationFileType migrationFileType,
  String brokerIpaCode,
  String zipVersion,
  String extractionTimestamp,
  int rowsToExtract
) {

  /**
   * Builds the base file name for the current export part.
   *
   * @param part the part number, starting from 1
   * @return the export base file name
   */
  public String buildExportBaseFileName(int part) {
    return "%s-%s-%s-part%d-%s".formatted(
      brokerIpaCode,
      migrationFileType.name(),
      extractionTimestamp,
      part,
      zipVersion
    );
  }

  /**
   * Resolves the CSV file path for the given export base file name.
   *
   * @param exportBaseFileName the base file name without extension
   * @return the CSV file path
   */
  public Path resolveCsvPath(String exportBaseFileName) {
    return extractionDirectory.resolve(exportBaseFileName + ".csv");
  }

  /**
   * Resolves the ZIP file path for the given export base file name.
   *
   * @param exportBaseFileName the base file name without extension
   * @return the ZIP file path
   */
  public Path resolveZipPath(String exportBaseFileName) {
    return extractionDirectory.resolve(exportBaseFileName + ".zip");
  }
}
