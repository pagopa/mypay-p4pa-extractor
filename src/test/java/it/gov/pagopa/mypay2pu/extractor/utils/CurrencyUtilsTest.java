package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrencyUtilsTest {

  @Test
  void toCentsShouldConvertAmountsAndPreserveNull() {
    assertEquals(150025L, CurrencyUtils.toCents(new BigDecimal("1500.25")));
    assertNull(CurrencyUtils.toCents(null));
  }
}
