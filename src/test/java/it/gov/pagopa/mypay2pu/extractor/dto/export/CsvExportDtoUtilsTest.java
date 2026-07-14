package it.gov.pagopa.mypay2pu.extractor.dto.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvExportDtoUtils Tests")
class CsvExportDtoUtilsTest {

  @Test
  @DisplayName("Should resolve ZIP version from valid DTO")
  void testResolveZipVersionSuccess() {
    // When
    String result = CsvExportDtoUtils.resolveZipVersion(new ValidCsvExportDto());

    // Then
    assertEquals("1.0.0", result);
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP version is null")
  void testResolveZipVersionNullValue() {
    // Given
    NullZipVersionDto dto = new NullZipVersionDto();
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> {
        CsvExportDtoUtils.resolveZipVersion(dto);
      }
    );
    assertTrue(exception.getMessage().contains("Invalid ZIP version"));
    assertTrue(exception.getMessage().contains("NullZipVersionDto"));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when ZIP version is blank")
  void testResolveZipVersionBlankValue() {
    // Given
    BlankZipVersionDto dto = new BlankZipVersionDto();
    // When & Then
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> {
        CsvExportDtoUtils.resolveZipVersion(dto);
      }
    );
    assertTrue(exception.getMessage().contains("Invalid ZIP version"));
    assertTrue(exception.getMessage().contains("BlankZipVersionDto"));
  }

  // Test fixtures

  static class ValidCsvExportDto implements CsvExportDto {
    @Override
    public String getZipVersion() {
      return "1.0.0";
    }
  }

  static class NullZipVersionDto implements CsvExportDto {
    @Override
    public String getZipVersion() {
      return null;
    }
  }

  static class BlankZipVersionDto implements CsvExportDto {
    @Override
    public String getZipVersion() {
      return "   ";
    }
  }
}
