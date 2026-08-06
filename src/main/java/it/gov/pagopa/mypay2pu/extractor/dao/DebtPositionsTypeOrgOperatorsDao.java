package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DebtPositionsTypeOrgOperatorsDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/debt-positions-type-org-operators/debt-positions-type-org-operators.sql";
  protected static final RowMapper<DebtPositionsTypeOrgOperators> ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPositionsTypeOrgOperators.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final String findByFiltersSql;

  public DebtPositionsTypeOrgOperatorsDao(@Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
                                          SqlLoader sqlLoader) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<DebtPositionsTypeOrgOperators> findByFilters(String ipaCode,
                                                           List<String> operatorFiscalCodes,
                                                           List<String> debtPositionTypeOrgCodes,
                                                           int limit,
                                                           int offset) {
    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, operatorFiscalCodes, debtPositionTypeOrgCodes, limit, offset),
      ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String ipaCode,
                                            List<String> operatorFiscalCodes,
                                            List<String> debtPositionTypeOrgCodes,
                                            int limit,
                                            int offset) {
    boolean operatorFiscalCodesEmpty = operatorFiscalCodes == null || operatorFiscalCodes.isEmpty();
    boolean debtPositionTypeOrgCodesEmpty = debtPositionTypeOrgCodes == null || debtPositionTypeOrgCodes.isEmpty();
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("operatorFiscalCodesEmpty", operatorFiscalCodesEmpty)
      .addValue("operatorFiscalCodes", operatorFiscalCodesEmpty ? Collections.singletonList(null) : operatorFiscalCodes)
      .addValue("debtPositionTypeOrgCodesEmpty", debtPositionTypeOrgCodesEmpty)
      .addValue("debtPositionTypeOrgCodes", debtPositionTypeOrgCodesEmpty ? Collections.singletonList(null) : debtPositionTypeOrgCodes);
  }
}
