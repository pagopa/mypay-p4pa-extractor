package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateTimeUtils {
  private DateTimeUtils() {}

  public static LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  public static LocalDateTime toStartOfNextDay(LocalDate date) {
    return date == null ? null : date.plusDays(1).atStartOfDay();
  }
}
