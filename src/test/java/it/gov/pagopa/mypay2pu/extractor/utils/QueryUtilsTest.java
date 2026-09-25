package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryUtilsTest {

  @Test
  void givenNullPaginationWhenBuildPaginatedFilterParamsThenReturnEmptyParams() {
    MapSqlParameterSource result = QueryUtils.buildPaginatedFilterParams(null, null);

    assertFalse(result.hasValue("limit"));
    assertFalse(result.hasValue("offset"));
  }

  @Test
  void givenPaginationWhenBuildPaginatedFilterParamsThenReturnParamsWithLimitAndOffset() {
    MapSqlParameterSource result = QueryUtils.buildPaginatedFilterParams(50, 100);

    assertEquals(50, result.getValue("limit"));
    assertEquals(100, result.getValue("offset"));
  }

  @Test
  void givenNullLimitOrOffsetWhenBuildPaginatedFilterParamsThenThrowIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.buildPaginatedFilterParams(null, 100)
    );

    assertEquals("limit and offset must be both provided", exception.getMessage());
  }

  @Test
  void givenInvalidLimitWhenBuildPaginatedFilterParamsThenThrowIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.buildPaginatedFilterParams(0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenInvalidOffsetWhenBuildPaginatedFilterParamsThenThrowIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.buildPaginatedFilterParams(1, -1)
    );

    assertEquals("offset must be non-negative", exception.getMessage());
  }

  @Test
  void givenDifferentLastExtractionDateAndDateFromWhenResolveDateFromThenThrowIllegalArgumentException() {
    OffsetDateTime lastExtractionDate = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0),
      ZoneOffset.UTC
    );
    OffsetDateTime dateFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 2, 0, 0),
      ZoneOffset.UTC
    );

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom)
    );

    assertEquals(
      "lastExtractionDate and filters.dateFrom must have the same value when both are provided",
      exception.getMessage()
    );
  }

  @Test
  void givenDatesWithDifferentOffsetsWhenResolveDateFromThenThrowIllegalArgumentException() {
    OffsetDateTime lastExtractionDate = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0),
      ZoneOffset.UTC
    );
    OffsetDateTime dateFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 1, 1, 0),
      ZoneOffset.ofHours(1)
    );

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom)
    );

    assertEquals(
      "lastExtractionDate and filters.dateFrom must have the same value when both are provided",
      exception.getMessage()
    );
  }

  @Test
  void givenPairedValuesWhenPairValuesThenPreserveTheirPositions() {
    List<Object[]> result = QueryUtils.pairValues(
      List.of("2024", "2025"),
      List.of("BOL001", "BOL002")
    );

    assertEquals(2, result.size());
    assertArrayEquals(new Object[]{"2024", "BOL001"}, result.get(0));
    assertArrayEquals(new Object[]{"2025", "BOL002"}, result.get(1));
  }

  @Test
  void givenListsWithDifferentSizesWhenPairValuesThenThrowIllegalArgumentException() {
    List<String> billYears = List.of("2024");
    List<String> billCodes = List.of("BOL001", "BOL002");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> QueryUtils.pairValues(billYears, billCodes)
    );

    assertEquals("logical key component lists must have the same size", exception.getMessage());
  }
}
