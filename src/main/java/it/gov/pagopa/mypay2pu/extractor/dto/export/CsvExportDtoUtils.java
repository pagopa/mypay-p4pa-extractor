package it.gov.pagopa.mypay2pu.extractor.dto.export;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for working with {@link CsvExportDto} implementations.
 */
public final class CsvExportDtoUtils {
  private static final String ZIP_VERSION_FIELD_NAME = "ZIP_VERSION";

  private CsvExportDtoUtils() {
    throw new AssertionError("Utility class cannot be instantiated");
  }

  public static String resolveZipVersion(Class<? extends CsvExportDto> dtoClass) {
    try {
      Field zipVersionField = dtoClass.getField(ZIP_VERSION_FIELD_NAME);
      if (!Modifier.isStatic(zipVersionField.getModifiers()) || zipVersionField.getType() != String.class) {
        throw new IllegalStateException(
          "Invalid public static ZIP_VERSION constant on DTO class " + dtoClass.getName()
        );
      }

      String zipVersion = (String) zipVersionField.get(null);
      if (zipVersion == null || zipVersion.isBlank()) {
        throw new IllegalStateException(
          "Invalid public static ZIP_VERSION constant on DTO class " + dtoClass.getName()
        );
      }
      return zipVersion;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(
        "Missing public static ZIP_VERSION constant on DTO class " + dtoClass.getName(),
        e
      );
    }
  }
}
