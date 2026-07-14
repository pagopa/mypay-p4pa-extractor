package it.gov.pagopa.mypay2pu.extractor.dto.export;

/**
 * Identifies a DTO that can be validated and serialized as an export CSV row.
 * Implementations expose the ZIP version associated with the exported file.
 *
 * See {@link CsvExportDtoUtils} for utility methods.
 */
public interface CsvExportDto {

  /**
   * Returns the ZIP format version used for the export.
   *
   * @return the ZIP version identifier
   */
  String getZipVersion();
}
