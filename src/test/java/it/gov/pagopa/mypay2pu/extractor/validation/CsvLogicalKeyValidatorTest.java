package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvLogicalKeyValidatorTest {

  private static final String INVALID_LOGICAL_KEY_MESSAGE =
    "filters.logicalKey must be a non-empty comma-separated list";
  private static final String EMPTY_VALUE_MESSAGE =
    "filters.logicalKey must not contain empty values";

  private final CsvLogicalKeyValidator validator = new CsvLogicalKeyValidator();

  @ParameterizedTest
  @MethodSource("parsedLogicalKeys")
  void givenParsableLogicalKeyWhenParseThenReturnTrimmedValues(String logicalKey, List<String> expectedValues) {
    List<String> result = CsvLogicalKeyValidator.parseLogicalKey(logicalKey);

    assertEquals(expectedValues, result);
  }

  private static Stream<Arguments> parsedLogicalKeys() {
    return Stream.of(
      Arguments.of(null, List.of()),
      Arguments.of("value", List.of("value")),
      Arguments.of("value1,value2", List.of("value1", "value2")),
      Arguments.of(" value1 , value2 ", List.of("value1", "value2"))
    );
  }

  @ParameterizedTest
  @MethodSource("invalidLogicalKeys")
  void givenInvalidLogicalKeyWhenParseThenThrowIllegalArgumentException(
    String logicalKey, String expectedMessage) {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> CsvLogicalKeyValidator.parseLogicalKey(logicalKey)
    );

    assertEquals(expectedMessage, exception.getMessage());
  }

  private static Stream<Arguments> invalidLogicalKeys() {
    return Stream.of(
      Arguments.of("", INVALID_LOGICAL_KEY_MESSAGE),
      Arguments.of(" ", INVALID_LOGICAL_KEY_MESSAGE),
      Arguments.of("value|other", INVALID_LOGICAL_KEY_MESSAGE),
      Arguments.of("value,", EMPTY_VALUE_MESSAGE),
      Arguments.of(",value", EMPTY_VALUE_MESSAGE),
      Arguments.of("value,,other", EMPTY_VALUE_MESSAGE),
      Arguments.of("value, ", EMPTY_VALUE_MESSAGE)
    );
  }

  @Test
  void givenValidRequestWhenValidateThenNoExceptionThrown() {
    assertDoesNotThrow(() -> validator.validate(requestWithLogicalKey(" value1 , value2 ")));
  }

  @ParameterizedTest
  @MethodSource("invalidLogicalKeys")
  void givenInvalidNonNullLogicalKeyRequestWhenValidateThenThrowBadRequestException(
    String logicalKey, String expectedMessage) {
    ExtractionRequest request = requestWithLogicalKey(logicalKey);
    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> validator.validate(request)
    );

    assertEquals("INVALID_EXTRACTION_FILTERS", exception.getCode());
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  void givenRequestWithoutFiltersWhenValidateThenNoExceptionIsThrown() {
    assertDoesNotThrow(
      () -> validator.validate(new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.DEBT_POSITIONS_TYPE, null, null))
    );
  }

  @Test
  void givenRequestWithNullLogicalKeyWhenValidateThenNoExceptionIsThrown() {
    assertDoesNotThrow(() -> validator.validate(requestWithLogicalKey(null)));
  }

  @Test
  void givenNullRequestWhenValidateThenNoExceptionIsThrown() {
    assertDoesNotThrow(() -> validator.validate(null));
  }

  private ExtractionRequest requestWithLogicalKey(String logicalKey) {
    return new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.DEBT_POSITIONS_TYPE,
      null,
      new ExtractionFilters().logicalKey(logicalKey)
    );
  }
}
