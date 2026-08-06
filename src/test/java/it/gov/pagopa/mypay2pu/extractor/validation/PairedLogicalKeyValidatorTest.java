package it.gov.pagopa.mypay2pu.extractor.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PairedLogicalKeyValidatorTest {

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
}
