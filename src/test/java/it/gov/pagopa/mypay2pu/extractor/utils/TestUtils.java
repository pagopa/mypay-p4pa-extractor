package it.gov.pagopa.mypay2pu.extractor.utils;

import java.util.TimeZone;

public class TestUtils {

  static {
    clearDefaultTimezone();
  }

  public static void clearDefaultTimezone() {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
  }
}
