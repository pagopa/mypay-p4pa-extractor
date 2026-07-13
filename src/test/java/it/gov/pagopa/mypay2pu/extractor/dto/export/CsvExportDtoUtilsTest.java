package it.gov.pagopa.mypay2pu.extractor.dto.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvExportDtoUtils Tests")
class CsvExportDtoUtilsTest {

  @Test
  @DisplayName("Should resolve ZIP_VERSION from valid DTO class")
  void testResolveZipVersionSuccess() {
    // When
    String result = CsvExportDtoUtils.resolveZipVersion(ValidCsvExportDto.class);

    // Then
    assertEquals("1.0.0", result);
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP_VERSION field is missing")
  void testResolveZipVersionMissingField() {
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> CsvExportDtoUtils.resolveZipVersion(MissingZipVersionFieldDto.class)
    );
    assertTrue(exception.getMessage().contains("Missing public static ZIP_VERSION constant"));
    assertTrue(exception.getMessage().contains("MissingZipVersionFieldDto"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP_VERSION field is missing (wrong name)")
  void testResolveZipVersionWrongFieldName() {
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> CsvExportDtoUtils.resolveZipVersion(WronglyNamedZipVersionDto.class)
    );
    assertTrue(exception.getMessage().contains("Missing public static ZIP_VERSION constant"));
    assertTrue(exception.getMessage().contains("WronglyNamedZipVersionDto"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP_VERSION is not String")
  void testResolveZipVersionNonStringField() {
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> CsvExportDtoUtils.resolveZipVersion(NonStringZipVersionDto.class)
    );
    assertTrue(exception.getMessage().contains("Invalid public static ZIP_VERSION constant"));
    assertTrue(exception.getMessage().contains("NonStringZipVersionDto"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP_VERSION is null")
  void testResolveZipVersionNullValue() {
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> CsvExportDtoUtils.resolveZipVersion(NullZipVersionDto.class)
    );
    assertTrue(exception.getMessage().contains("Invalid public static ZIP_VERSION constant"));
    assertTrue(exception.getMessage().contains("NullZipVersionDto"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP_VERSION is blank")
  void testResolveZipVersionBlankValue() {
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> CsvExportDtoUtils.resolveZipVersion(BlankZipVersionDto.class)
    );
    assertTrue(exception.getMessage().contains("Invalid public static ZIP_VERSION constant"));
    assertTrue(exception.getMessage().contains("BlankZipVersionDto"));
  }

  // Test fixtures

  static class ValidCsvExportDto implements CsvExportDto {
    public static final String ZIP_VERSION = "1.0.0";
  }

  static class MissingZipVersionFieldDto implements CsvExportDto {
  }

  static class WronglyNamedZipVersionDto implements CsvExportDto {
    public final String zipVersion = "1.0.0";
  }

  static class NonStringZipVersionDto implements CsvExportDto {
    public static final Integer ZIP_VERSION = 1;
  }

  static class NullZipVersionDto implements CsvExportDto {
    public static final String ZIP_VERSION = null;
  }

  static class BlankZipVersionDto implements CsvExportDto {
    public static final String ZIP_VERSION = "   ";
  }
}
