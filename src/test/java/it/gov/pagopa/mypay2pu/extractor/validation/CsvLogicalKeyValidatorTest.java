package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvLogicalKeyValidatorTest {

  private final CsvLogicalKeyValidator validator = new CsvLogicalKeyValidator();

  @ParameterizedTest
  @ValueSource(strings = {"value", "value1,value2", " value1 , value2 "})
  void givenValidLogicalKeyWhenParseThenReturnTrimmedValues(String logicalKey) {
    List<String> result = CsvLogicalKeyValidator.parseLogicalKey(logicalKey);

    List<String> twoValuesList = List.of("value1", "value2");
    List<String> singletonList = List.of("value");
    assertEquals(
      logicalKey.contains(",") ? twoValuesList : singletonList,
      result
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "value|other", "value,", ",value", "value,,other"})
  void givenInvalidLogicalKeyWhenParseThenThrowIllegalArgumentException(String logicalKey) {
    assertThrows(IllegalArgumentException.class, () -> CsvLogicalKeyValidator.parseLogicalKey(logicalKey));
  }

  @Test
  void givenValidRequestWhenValidateThenNoExceptionThrown() {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.DEBT_POSITIONS_TYPE,
      null,
      new ExtractionFilters().logicalKey(" value1 , value2 ")
    );

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "value|other", "value, ", "value,,other"})
  void givenInvalidRequestWhenValidateThenThrowBadRequestException(String logicalKey) {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.DEBT_POSITIONS_TYPE,
      null,
      new ExtractionFilters().logicalKey(logicalKey)
    );

    BadRequestException exception = assertThrows(BadRequestException.class, () -> validator.validate(request));

    assertEquals("INVALID_EXTRACTION_FILTERS", exception.getCode());
  }

  @Test
  void givenNullRequestWhenValidateThenThrowBadRequestException() {
    BadRequestException exception = assertThrows(BadRequestException.class, () -> validator.validate(null));

    assertEquals("INVALID_EXTRACTION_FILTERS", exception.getCode());
  }
}
