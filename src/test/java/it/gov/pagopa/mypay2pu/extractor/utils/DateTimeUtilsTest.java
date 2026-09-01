package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateTimeUtilsTest {

  @Test
  void givenNullDateWhenToLocalDateTimeThenReturnNull() {
    assertNull(DateTimeUtils.toLocalDateTime(null));
  }

  @Test
  void givenDateWhenToLocalDateTimeThenReturnLocalDateTime() {
    OffsetDateTime input = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 2, 11, 15),
      ZoneOffset.ofHours(1)
    );
    assertEquals(
      LocalDateTime.of(2026, Month.JANUARY, 2, 11, 15),
      DateTimeUtils.toLocalDateTime(input)
    );
  }
}
