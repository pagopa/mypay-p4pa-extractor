package it.gov.pagopa.mypay2pu.extractor.utils;

import java.math.BigDecimal;

public final class CurrencyUtils {

  private CurrencyUtils() {
  }

  public static Long toCents(BigDecimal value) {
    return value == null ? null : value.movePointRight(2).longValueExact();
  }
}
