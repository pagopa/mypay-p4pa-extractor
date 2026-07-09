package it.gov.pagopa.mypay2pu.extractor.utils;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

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
}
