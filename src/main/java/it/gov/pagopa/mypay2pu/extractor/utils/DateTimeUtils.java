package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class DateTimeUtils {
  private DateTimeUtils() {}

  public static LocalDateTime toStartOfDay(OffsetDateTime date) {
    return date == null ? null : date.toLocalDateTime().toLocalDate().atStartOfDay();
  }

  public static LocalDateTime toStartOfNextDay(OffsetDateTime date) {
    return date == null ? null : date.toLocalDateTime().toLocalDate().plusDays(1).atStartOfDay();
  }
}
