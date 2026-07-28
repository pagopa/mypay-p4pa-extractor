package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Repository
public class DebtPositionTypeOrgDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/debt-position-type-org/debt-position-type-org.sql";
  private static final String IS_EXTERNAL_SQL_PATH = "mypivot/debt-position-type-org/is-external.sql";
  protected static final RowMapper<DebtPositionTypeOrg> DEBT_POSITION_TYPE_ORG_ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPositionTypeOrg.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByFiltersSql;
  private final String isExternalSql;

  public DebtPositionTypeOrgDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    @Autowired(required = false) @Qualifier("mypivotNamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
    this.isExternalSql = sqlLoader.load(IS_EXTERNAL_SQL_PATH);
  }

  public List<DebtPositionTypeOrg> findByFilters(String ipaCode,
                                                 List<String> debtPositionTypeOrgCodes,
                                                 int limit,
                                                 int offset) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }

    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, debtPositionTypeOrgCodes, limit, offset),
      DEBT_POSITION_TYPE_ORG_ROW_MAPPER
    );
  }

  public boolean isExternal(String ipaCode, String debtPositionTypeOrgCode) {
    if (mypivotJdbcTemplate == null || !StringUtils.hasText(ipaCode)
      || !StringUtils.hasText(debtPositionTypeOrgCode)) {
      return false;
    }

    Boolean exists = mypivotJdbcTemplate.queryForObject(
      isExternalSql,
      new MapSqlParameterSource()
        .addValue("ipaCode", ipaCode)
        .addValue("debtPositionsTypeOrgCode", debtPositionTypeOrgCode),
      Boolean.class
    );
    return Boolean.TRUE.equals(exists);
  }

  private MapSqlParameterSource buildParams(String ipaCode,
                                            List<String> debtPositionTypeOrgCodes,
                                            int limit,
                                            int offset) {
    boolean isEmptyCollection = CollectionUtils.isEmpty(debtPositionTypeOrgCodes);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("skipDebtPositionTypeOrgCodesFilter", isEmptyCollection)
      .addValue("debtPositionTypeOrgCodes", isEmptyCollection ? Collections.singletonList(null) : debtPositionTypeOrgCodes);
  }
}
