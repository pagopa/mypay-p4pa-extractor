package it.gov.pagopa.mypay2pu.extractor.mapper.debtposition;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DebtPositionPersonEntityTypeCsvConverterTest {

  @Test
  void toCsvValueShouldConvertDirectEnumValues() {
    assertEquals(PersonEntityType.F, DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue("F"));
    assertEquals(PersonEntityType.G, DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue("G"));
  }

  @Test
  void toCsvValueShouldReturnNullWhenSourceValueIsNull() {
    assertNull(DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue(null));
  }

  @Test
  void toCsvValueShouldThrowOnUnknownEntityType() {
    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue("X")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("entityType", exception.getField());
    assertEquals("X", exception.getRejectedValue());
  }
}
