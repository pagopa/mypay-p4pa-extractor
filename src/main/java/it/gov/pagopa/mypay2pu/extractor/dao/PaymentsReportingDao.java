package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PaymentsReportingDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "fesp/payments-reporting/payments-reporting.sql";
  protected static final RowMapper<Path> PAYMENTS_REPORTING_FILE_ROW_MAPPER = (resultSet, rowNum) ->
    Path.of(resultSet.getString("de_nome_file_scaricato"));

  private final NamedParameterJdbcTemplate fespJdbcTemplate;
  private final String findByFiltersSql;

  public PaymentsReportingDao(
    @Qualifier("fespNamedParameterJdbcTemplate") NamedParameterJdbcTemplate fespJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.fespJdbcTemplate = fespJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    LocalDateTime dateFrom,
    LocalDateTime dateTo
  ) {
    return findByDateRange(ipaCode, null, dateFrom, dateTo, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    LocalDateTime lastExtractionDate,
    LocalDateTime dateFrom,
    LocalDateTime dateTo
  ) {
    return findByDateRange(ipaCode, lastExtractionDate, dateFrom, dateTo, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByDateRange(
    String ipaCode,
    LocalDateTime lastExtractionDate,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    int limit,
    int offset
  ) {
    validateIpaCode(ipaCode);
    return fespJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom), dateTo, null, limit, offset),
      PAYMENTS_REPORTING_FILE_ROW_MAPPER
    );
  }

  public List<Path> findByLogicalKey(String ipaCode, String logicalKey) {
    return findByLogicalKey(ipaCode, logicalKey, Integer.MAX_VALUE, 0);
  }

  public List<Path> findByLogicalKey(String ipaCode, String logicalKey, int limit, int offset) {
    validateIpaCode(ipaCode);
    if (!StringUtils.hasText(logicalKey)) {
      throw new IllegalArgumentException("logicalKey must not be blank");
    }
    return fespJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, null, null, logicalKey, limit, offset),
      PAYMENTS_REPORTING_FILE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String ipaCode,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    String logicalKey,
    int limit,
    int offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("dateFrom", dateFrom)
      .addValue("skipDateFromFilter", dateFrom == null)
      .addValue("dateTo", dateTo)
      .addValue("skipDateToFilter", dateTo == null)
      .addValue("skipLogicalKeyFilter", logicalKey == null)
      .addValue("logicalKey", logicalKey);
  }

  private void validateIpaCode(String ipaCode) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }
  }
}
