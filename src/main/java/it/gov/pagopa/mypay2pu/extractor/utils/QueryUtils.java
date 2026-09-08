package it.gov.pagopa.mypay2pu.extractor.utils;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.OffsetDateTime;

public class QueryUtils {
  private QueryUtils() {}

  public static MapSqlParameterSource buildPaginatedFilterParams(Integer limit, Integer offset) {
    if (limit == null && offset == null) {
      return new MapSqlParameterSource();
    }
    if (limit == null || offset == null) {
      throw new IllegalArgumentException("limit and offset must be both provided");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be greater than 0");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be non-negative");
    }
    return new MapSqlParameterSource()
      .addValue("limit", limit)
      .addValue("offset", offset);
  }

  public static OffsetDateTime resolveDateFrom(OffsetDateTime lastExtractionDate, OffsetDateTime dateFrom) {
    if (hasConflictingDates(lastExtractionDate, dateFrom)) {
      throw new IllegalArgumentException(
        "lastExtractionDate and filters.dateFrom must have the same value when both are provided"
      );
    }
    return dateFrom != null ? dateFrom : lastExtractionDate;
  }

  /**
   * Checks whether two supplied dates identify different instants, irrespective of their offsets.
   *
   * @param lastExtractionDate incremental extraction timestamp
   * @param dateFrom explicit extraction start timestamp
   * @return {@code true} when both dates are present and identify different instants
   */
  public static boolean hasConflictingDates(OffsetDateTime lastExtractionDate, OffsetDateTime dateFrom) {
    return lastExtractionDate != null
      && dateFrom != null
      && lastExtractionDate.toInstant().compareTo(dateFrom.toInstant()) != 0;
  }
}
