package it.gov.pagopa.mypay2pu.extractor.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvLogicalKeyValidatorTest {

  @ParameterizedTest
  @ValueSource(strings = {"value", "value1,value2", " value1 , value2 "})
  void givenValidLogicalKeyWhenParseThenReturnTrimmedValues(String logicalKey) {
    List<String> result = CsvLogicalKeyValidator.parseLogicalKey(logicalKey);

    assertEquals(
      logicalKey.contains(",") ? List.of("value1", "value2") : List.of("value"),
      result
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "value|other", "value,", ",value", "value,,other"})
  void givenInvalidLogicalKeyWhenParseThenThrowIllegalArgumentException(String logicalKey) {
    assertThrows(IllegalArgumentException.class, () -> CsvLogicalKeyValidator.parseLogicalKey(logicalKey));
  }
}
