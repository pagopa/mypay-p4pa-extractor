package it.gov.pagopa.mypay2pu.extractor.dto.export;

/**
 * Utility class for working with {@link CsvExportDto} instances.
 */
public final class CsvExportDtoUtils {
  private CsvExportDtoUtils() {
    throw new AssertionError("Utility class cannot be instantiated");
  }

  /**
   * Resolves the ZIP version from the given DTO.
   *
   * @param dto the export DTO
   * @return the ZIP version
   * @throws IllegalStateException if the DTO returns a null or blank ZIP version
   */
  public static String resolveZipVersion(CsvExportDto dto) {
    String zipVersion = dto.getZipVersion();
    if (zipVersion == null || zipVersion.isBlank()) {
      throw new IllegalStateException(
        "Invalid ZIP version on DTO class " + dto.getClass().getName()
      );
    }
    return zipVersion;
  }
}
