package it.gov.pagopa.mypay2pu.extractor.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UtilitiesTest {

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  @Test
  void givenLocalDateWhenConvertingToXmlGregorianCalendarThenPreserveDate() {
    Assertions.assertEquals(
      "2026-01-15",
      Utilities.toXMLGregorianCalendar(LocalDate.of(2026, Month.JANUARY, 15)).toXMLFormat()
    );
    Assertions.assertNull(Utilities.toXMLGregorianCalendar((LocalDate) null));
  }

  @Test
  void givenLocalDateTimeWhenConvertingToXmlGregorianCalendarThenPreserveSecondsWithoutFraction() {
    Assertions.assertEquals(
      "2026-01-15T10:30:45",
      Utilities.toXMLGregorianCalendar(LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30, 45, 123_000_000))
        .toXMLFormat()
    );
    Assertions.assertNull(Utilities.toXMLGregorianCalendar((LocalDateTime) null));
  }

  @Test
  void givenOffsetDateTimeWhenConvertingToXmlGregorianCalendarThenPreserveOffset() {
    Assertions.assertEquals(
      "2026-01-15T10:30:45+01:00",
      Utilities.toXMLGregorianCalendar(
        LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30, 45).atOffset(ZoneOffset.ofHours(1))
      ).toXMLFormat()
    );
    Assertions.assertNull(Utilities.toXMLGregorianCalendar((OffsetDateTime) null));
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }
}
