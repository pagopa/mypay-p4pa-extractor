package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.List;

@Repository
public class PaymentsReportingDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/payments-reporting/payments-reporting.sql";
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
    String organizationId,
    LocalDateTime dateFrom,
    LocalDateTime dateTo
  ) {
    return findByDateRange(organizationId, null, dateFrom, dateTo);
  }

  public List<Path> findByDateRange(
    String organizationId,
    LocalDateTime lastExtractionDate,
    LocalDateTime dateFrom,
    LocalDateTime dateTo
  ) {
    validateOrganizationId(organizationId);
    return fespJdbcTemplate.query(
      findByFiltersSql,
      buildParams(organizationId, lastExtractionDate, dateFrom, dateTo, null),
      PAYMENTS_REPORTING_FILE_ROW_MAPPER
    );
  }

  public List<Path> findByLogicalKey(String organizationId, String logicalKey) {
    validateOrganizationId(organizationId);
    if (!StringUtils.hasText(logicalKey)) {
      throw new IllegalArgumentException("logicalKey must not be blank");
    }
    return fespJdbcTemplate.query(
      findByFiltersSql,
      buildParams(organizationId, null, null, null, logicalKey),
      PAYMENTS_REPORTING_FILE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String organizationId,
    LocalDateTime lastExtractionDate,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    String logicalKey
  ) {
    return new MapSqlParameterSource()
      .addValue("organizationId", organizationId)
      .addValue("lastExtractionDate", lastExtractionDate)
      .addValue("dateFrom", dateFrom)
      .addValue("dateTo", dateTo)
      .addValue("skipLogicalKeyFilter", logicalKey == null)
      .addValue("logicalKey", logicalKey);
  }

  private void validateOrganizationId(String organizationId) {
    if (!StringUtils.hasText(organizationId)) {
      throw new IllegalArgumentException("organizationId must not be blank");
    }
  }
}
