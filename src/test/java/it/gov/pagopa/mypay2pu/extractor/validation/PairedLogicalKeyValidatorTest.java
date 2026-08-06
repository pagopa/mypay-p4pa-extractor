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

class PairedLogicalKeyValidatorTest {

  private static final String EMPTY_PAIR_MESSAGE =
    "filters.logicalKey must contain two non-empty comma-separated lists";
  private static final String INVALID_SEPARATOR_MESSAGE =
    "filters.logicalKey must contain exactly one vertical bar";
  private static final String EMPTY_VALUE_MESSAGE =
    "filters.logicalKey must not contain empty values";

  private final PairedLogicalKeyValidator validator = new PairedLogicalKeyValidator();

  @ParameterizedTest
  @MethodSource("parsedLogicalKeys")
  void givenParsableLogicalKeyWhenParseThenReturnBothTrimmedLists(
    String logicalKey, LogicalKeyPair expectedPair) {
    LogicalKeyPair result = PairedLogicalKeyValidator.parseLogicalKey(logicalKey);

    assertEquals(expectedPair, result);
  }

  private static Stream<Arguments> parsedLogicalKeys() {
    return Stream.of(
      Arguments.of(null, new LogicalKeyPair(List.of(), List.of())),
      Arguments.of("left|right", new LogicalKeyPair(List.of("left"), List.of("right"))),
      Arguments.of(" value1 , value2 | value3,value4 ",
        new LogicalKeyPair(List.of("value1", "value2"), List.of("value3", "value4")))
    );
  }

  @ParameterizedTest
  @MethodSource("invalidLogicalKeys")
  void givenInvalidLogicalKeyWhenParseThenThrowIllegalArgumentException(
    String logicalKey, String expectedMessage) {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> PairedLogicalKeyValidator.parseLogicalKey(logicalKey)
    );

    assertEquals(expectedMessage, exception.getMessage());
  }

  private static Stream<Arguments> invalidLogicalKeys() {
    return Stream.of(
      Arguments.of("", EMPTY_PAIR_MESSAGE),
      Arguments.of(" ", EMPTY_PAIR_MESSAGE),
      Arguments.of("value", INVALID_SEPARATOR_MESSAGE),
      Arguments.of("value|other|third", INVALID_SEPARATOR_MESSAGE),
      Arguments.of("value|", "filters.logicalKey must be a non-empty comma-separated list"),
      Arguments.of("|value", "filters.logicalKey must be a non-empty comma-separated list"),
      Arguments.of("value,|other", EMPTY_VALUE_MESSAGE),
      Arguments.of("value|other,", EMPTY_VALUE_MESSAGE)
    );
  }

  @Test
  void givenValidRequestWhenValidateThenNoExceptionThrown() {
    assertDoesNotThrow(() -> validator.validate(requestWithLogicalKey("value1,value2|value3,value4")));
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
      () -> validator.validate(
        new ExtractionRequest(
          List.of("IPA_CODE"),
          MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS,
          null,
          null
        )
      )
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
      MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS,
      null,
      new ExtractionFilters().logicalKey(logicalKey)
    );
  }
}
