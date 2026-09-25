package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentReportingMyPayShareDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "fesp/payments-reporting/payments-reporting.sql";
  protected static final RowMapper<Path> PAYMENTS_REPORTING_FILE_ROW_MAPPER = (resultSet, rowNum) ->
    Path.of(resultSet.getString("de_nome_file_scaricato"));

  private final NamedParameterJdbcTemplate fespJdbcTemplate;
  private final String findByFiltersSql;

  public PaymentReportingMyPayShareDao(
    @Qualifier("fespNamedParameterJdbcTemplate") NamedParameterJdbcTemplate fespJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.fespJdbcTemplate = fespJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo
  ) {
    return findByDateRange(ipaCode, null, dateFrom, dateTo, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo
  ) {
    return findByDateRange(ipaCode, lastExtractionDate, dateFrom, dateTo, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo,
    int limit,
    int offset
  ) {
    validateIpaCode(ipaCode);
    return findByFilters(ipaCode, lastExtractionDate, dateFrom, dateTo, null, limit, offset);
  }

  public List<Path> findByFlowIdentifiers(String ipaCode, List<String> flowIdentifiers) {
    return findByFlowIdentifiers(ipaCode, flowIdentifiers, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByFlowIdentifiers(String ipaCode, List<String> flowIdentifiers, int limit, int offset) {
    return findByFilters(ipaCode, null, null, null, flowIdentifiers, limit, offset);
  }

  public List<Path> findByFilters(String ipaCode,
                                  OffsetDateTime lastExtractionDate,
                                  OffsetDateTime dateFrom,
                                  OffsetDateTime dateTo,
                                  List<String> flowIdentifiers,
                                  int limit,
                                  int offset) {
    validateIpaCode(ipaCode);
    return fespJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom), dateTo, flowIdentifiers, limit, offset),
      PAYMENTS_REPORTING_FILE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String ipaCode,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo,
    List<String> flowIdentifiers,
    int limit,
    int offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("dateFrom", dateFrom)
      .addValue("skipDateFromFilter", dateFrom == null)
      .addValue("dateTo", dateTo)
      .addValue("skipDateToFilter", dateTo == null)
      .addValue("skipFlowIdentifiersFilter", flowIdentifiers == null || flowIdentifiers.isEmpty())
      .addValue("flowIdentifiers", flowIdentifiers == null || flowIdentifiers.isEmpty()
        ? Collections.singletonList(null)
        : flowIdentifiers);
  }

  private void validateIpaCode(String ipaCode) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }
  }
}
