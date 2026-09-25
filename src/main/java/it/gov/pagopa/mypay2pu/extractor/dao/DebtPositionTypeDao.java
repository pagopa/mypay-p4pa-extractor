package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Repository
public class DebtPositionTypeDao {

  protected static final String FIND_BY_FILTERS_SQL_PATH = "mypay/debt-position-type/debt-position-type.sql";
  protected static final RowMapper<DebtPositionType> DEBT_POSITIONS_TYPE_ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPositionType.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final ExtractorExportProperties exportProperties;
  private final String findByFiltersSql;

  public DebtPositionTypeDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    ExtractorExportProperties exportProperties,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.exportProperties = exportProperties;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<DebtPositionType> findByFilters(List<String> debtPositionTypeCodes,
                                             int limit,
                                             int offset) {
    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(debtPositionTypeCodes, limit, offset),
      DEBT_POSITIONS_TYPE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(List<String> debtPositionTypeCodes,
                                            int limit,
                                            int offset) {
    boolean isEmptyCollection = CollectionUtils.isEmpty(debtPositionTypeCodes);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("brokerCf", exportProperties.brokerCf())
      .addValue("skipDebtPositionTypeCodesFilter", isEmptyCollection)
      .addValue("debtPositionTypeCodes", isEmptyCollection ? Collections.singletonList(null) : debtPositionTypeCodes);
  }
}
