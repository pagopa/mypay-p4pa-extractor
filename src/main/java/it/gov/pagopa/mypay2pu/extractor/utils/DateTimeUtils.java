package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class DateTimeUtils {
  private DateTimeUtils() {}

  public static LocalDateTime toLocalDateTime(OffsetDateTime date) {
    return date == null ? null : date.toLocalDateTime();
  }

  public static LocalDateTime toLocalDateTimeExclusive(OffsetDateTime date) {
    return toLocalDateTime(date);
  }
}
