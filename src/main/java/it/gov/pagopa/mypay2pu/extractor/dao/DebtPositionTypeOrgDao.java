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

import java.util.Collections;
import java.util.List;

@Repository
public class DebtPositionTypeOrgDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/debt-position-type-org/debt-position-type-org.sql";
  private static final String IS_EXTERNAL_SQL_PATH = "mypivot/debt-position-type-org/is-external.sql";
  private static final List<String> NULL_LOGICAL_KEY = Collections.singletonList(null);
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
                                                 List<String> debtPositionsTypeOrgCodes,
                                                 int limit,
                                                 int offset) {
    if (ipaCode == null || ipaCode.isBlank()) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }

    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, debtPositionsTypeOrgCodes, limit, offset),
      DEBT_POSITION_TYPE_ORG_ROW_MAPPER
    );
  }

  public boolean isExternal(String organizationId, String debtPositionsTypeOrgCode) {
    if (mypivotJdbcTemplate == null || organizationId == null || organizationId.isBlank()
      || debtPositionsTypeOrgCode == null || debtPositionsTypeOrgCode.isBlank()) {
      return false;
    }

    Boolean exists = mypivotJdbcTemplate.queryForObject(
      isExternalSql,
      new MapSqlParameterSource()
        .addValue("organizationId", organizationId)
        .addValue("debtPositionsTypeOrgCode", debtPositionsTypeOrgCode),
      Boolean.class
    );
    return Boolean.TRUE.equals(exists);
  }

  private MapSqlParameterSource buildParams(String organizationId,
                                            List<String> debtPositionsTypeOrgCodes,
                                            int limit,
                                            int offset) {
    boolean logicalKeysEmpty = debtPositionsTypeOrgCodes == null || debtPositionsTypeOrgCodes.isEmpty();
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("organizationId", organizationId)
      .addValue("logicalKeysEmpty", logicalKeysEmpty)
      .addValue("debtPositionsTypeOrgCodes", logicalKeysEmpty ? NULL_LOGICAL_KEY : debtPositionsTypeOrgCodes);
  }
}
