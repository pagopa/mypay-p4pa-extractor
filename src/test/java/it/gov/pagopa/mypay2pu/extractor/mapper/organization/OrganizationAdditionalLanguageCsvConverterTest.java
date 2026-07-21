package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationAdditionalLanguage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationAdditionalLanguageCsvConverterTest {

  @Test
  void toCsvValueShouldConvertDirectEnumValues() {
    assertEquals(OrganizationAdditionalLanguage.EN, OrganizationAdditionalLanguageCsvConverter.INSTANCE.toCsvValue("EN"));
    assertEquals(OrganizationAdditionalLanguage.FR, OrganizationAdditionalLanguageCsvConverter.INSTANCE.toCsvValue("FR"));
    assertEquals(OrganizationAdditionalLanguage.DE, OrganizationAdditionalLanguageCsvConverter.INSTANCE.toCsvValue("DE"));
  }

  @Test
  void toCsvValueShouldReturnNullWhenSourceValueIsNull() {
    assertNull(OrganizationAdditionalLanguageCsvConverter.INSTANCE.toCsvValue(null));
  }

  @Test
  void toCsvValueShouldThrowOnUnknownLanguage() {
    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> OrganizationAdditionalLanguageCsvConverter.INSTANCE.toCsvValue("IT")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("additionalLanguage", exception.getField());
    assertEquals("IT", exception.getRejectedValue());
  }
}
