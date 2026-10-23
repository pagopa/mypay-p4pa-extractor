package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentReportingMyPivotDao {

  protected static final String FIND_FLOW_IDENTIFIERS_SQL_PATH =
    "mypivot/payments-reporting/payments-reporting-flow.sql";
  protected static final String FIND_ROWS_BY_FLOW_IDENTIFIER_SQL_PATH =
    "mypivot/payments-reporting/payments-reporting-flow-rows.sql";
  protected static final RowMapper<PaymentReporting> PAYMENT_REPORTING_ROW_MAPPER =
    DataClassRowMapper.newInstance(PaymentReporting.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findFlowIdentifiersSql;
  private final String findRowsByFlowIdentifierSql;

  public PaymentReportingMyPivotDao(
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findFlowIdentifiersSql = sqlLoader.load(FIND_FLOW_IDENTIFIERS_SQL_PATH);
    this.findRowsByFlowIdentifierSql = sqlLoader.load(FIND_ROWS_BY_FLOW_IDENTIFIER_SQL_PATH);
  }

  public List<String> findFlowIdentifiers(
    String ipaCode,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo,
    List<String> flowIdentifiers,
    int limit,
    int offset
  ) {
    validateIpaCodeAndDatasource(ipaCode);
    return mypivotJdbcTemplate.queryForList(
      findFlowIdentifiersSql,
      buildFilterParams(ipaCode, lastExtractionDate, dateFrom, dateTo, flowIdentifiers, limit, offset),
      String.class
    );
  }

  public List<PaymentReporting> findRowsByFlowIdentifier(
    String ipaCode,
    String flowIdentifier,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo
  ) {
    validateIpaCodeAndDatasource(ipaCode);
    if (!StringUtils.hasText(flowIdentifier)) {
      throw new IllegalArgumentException("flowIdentifier must not be blank");
    }
    return mypivotJdbcTemplate.query(
      findRowsByFlowIdentifierSql,
      buildFilterParams(ipaCode, lastExtractionDate, dateFrom, dateTo, List.of(flowIdentifier), Integer.MAX_VALUE, 0),
      PAYMENT_REPORTING_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildFilterParams(
    String ipaCode,
    OffsetDateTime lastExtractionDate,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo,
    List<String> flowIdentifiers,
    int limit,
    int offset
  ) {
    OffsetDateTime resolvedDateFrom = QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom);
    boolean skipFlowIdentifiersFilter = flowIdentifiers == null || flowIdentifiers.isEmpty();
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("skipCreatedFromFilter", resolvedDateFrom == null)
      .addValue("createdFrom", DateTimeUtils.toLocalDateTime(resolvedDateFrom))
      .addValue("skipCreatedToFilter", dateTo == null)
      .addValue("createdTo", DateTimeUtils.toLocalDateTime(dateTo))
      .addValue("skipFlowIdentifiersFilter", skipFlowIdentifiersFilter)
      .addValue("flowIdentifiers", skipFlowIdentifiersFilter ? Collections.singletonList(null) : flowIdentifiers);
  }

  private void validateIpaCodeAndDatasource(String ipaCode) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }
    if (mypivotJdbcTemplate == null) {
      throw new IllegalStateException("MyPivot datasource must be enabled for payments reporting extraction");
    }
  }
}
