package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationStatusCsvConverterTest {

  @Test
  void toCsvValueShouldConvertKnownStatuses() {
    assertEquals(OrganizationStatus.ACTIVE, OrganizationStatusCsvConverter.INSTANCE.toCsvValue("ESERCIZIO"));
    assertEquals(OrganizationStatus.DRAFT, OrganizationStatusCsvConverter.INSTANCE.toCsvValue("INSERITO"));
    assertEquals(OrganizationStatus.DRAFT, OrganizationStatusCsvConverter.INSTANCE.toCsvValue("PRE-ESERCIZIO"));
  }

  @Test
  void toCsvValueShouldReturnNullWhenSourceValueIsNull() {
    assertNull(OrganizationStatusCsvConverter.INSTANCE.toCsvValue(null));
  }

  @Test
  void toCsvValueShouldThrowOnUnknownStatus() {
    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> OrganizationStatusCsvConverter.INSTANCE.toCsvValue("UNKNOWN")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("status", exception.getField());
    assertEquals("UNKNOWN", exception.getRejectedValue());
  }
}
