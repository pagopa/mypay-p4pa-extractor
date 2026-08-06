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

class PairedLogicalKeyValidatorTest {

  private final PairedLogicalKeyValidator validator = new PairedLogicalKeyValidator();

  @Test
  void givenValidLogicalKeyWhenParseThenReturnBothTrimmedLists() {
    LogicalKeyPair result = PairedLogicalKeyValidator.parseLogicalKey(" value1 , value2 | value3,value4 ");

    assertEquals(List.of("value1", "value2"), result.left());
    assertEquals(List.of("value3", "value4"), result.right());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "value", "value|", "|value", "value|other|third", "value,|other", "value|other,"})
  void givenInvalidLogicalKeyWhenParseThenThrowIllegalArgumentException(String logicalKey) {
    assertThrows(IllegalArgumentException.class, () -> PairedLogicalKeyValidator.parseLogicalKey(logicalKey));
  }

  @Test
  void givenValidRequestWhenValidateThenNoExceptionThrown() {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS,
      null,
      new ExtractionFilters().logicalKey("value1,value2|value3,value4")
    );

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "value", "value|", "|value", "value|other|third", "value,|other", "value|other,"})
  void givenInvalidRequestWhenValidateThenThrowBadRequestException(String logicalKey) {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS,
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
