package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateTimeUtilsTest {

  @Test
  void givenNullDateWhenToStartOfDayThenReturnNull() {
    assertNull(DateTimeUtils.toStartOfDay(null));
  }

  @Test
  void givenDateWhenToStartOfDayThenReturnStartOfDay() {
    assertEquals(
      LocalDate.of(2026, Month.JANUARY, 2).atStartOfDay(),
      DateTimeUtils.toStartOfDay(OffsetDateTime.of(2026, 1, 2, 0, 0, 0, 0, OffsetDateTime.now().getOffset()))
    );
  }

  @Test
  void givenNullDateWhenToStartOfNextDayThenReturnNull() {
    assertNull(DateTimeUtils.toStartOfNextDay(null));
  }

  @Test
  void givenDateWhenToStartOfNextDayThenReturnNextDayStartOfDay() {
    assertEquals(
      LocalDate.of(2026, Month.JANUARY, 3).atStartOfDay(),
      DateTimeUtils.toStartOfNextDay(OffsetDateTime.of(2026, 1, 2, 0, 0, 0, 0, OffsetDateTime.now().getOffset()))
    );
  }
}
