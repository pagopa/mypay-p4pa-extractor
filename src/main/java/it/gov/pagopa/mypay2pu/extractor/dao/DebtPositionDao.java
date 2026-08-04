package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DebtPositionDao {

  private static final String FIND_DEBT_POSITIONS_SQL_PATH = "mypay/debt-positions/debt-positions-open.sql";
  private static final String FIND_CANCELLED_DEBT_POSITIONS_SQL_PATH = "mypay/debt-positions/debt-positions-cancelled.sql";
  protected static final RowMapper<DebtPosition> DEBT_POSITION_ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPosition.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final String findDebtPositionsSql;
  private final String findCancelledDebtPositionsSql;

  public DebtPositionDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.findDebtPositionsSql = sqlLoader.load(FIND_DEBT_POSITIONS_SQL_PATH);
    this.findCancelledDebtPositionsSql = sqlLoader.load(FIND_CANCELLED_DEBT_POSITIONS_SQL_PATH);
  }

  public List<DebtPosition> findDebtPositions(
    String codIpaEnte,
    String gpdIupd,
    String codIud,
    ExtractionFilters filters,
    int limit,
    int offset
  ) {
    return findByFilters(findDebtPositionsSql, codIpaEnte, gpdIupd, codIud, filters, limit, offset);
  }

  public List<DebtPosition> findCancelledDebtPositions(
    String codIpaEnte,
    String gpdIupd,
    String codIud,
    ExtractionFilters filters,
    int limit,
    int offset
  ) {
    return findByFilters(findCancelledDebtPositionsSql, codIpaEnte, gpdIupd, codIud, filters, limit, offset);
  }

  private List<DebtPosition> findByFilters(
    String sql,
    String codIpaEnte,
    String gpdIupd,
    String codIud,
    ExtractionFilters filters,
    int limit,
    int offset
  ) {
    if (StringUtils.isEmpty(codIpaEnte)) {
      throw new IllegalArgumentException("codIpaEnte must not be blank");
    }
    return mp4JdbcTemplate.query(
      sql,
      buildParams(codIpaEnte, gpdIupd, codIud, filters, limit, offset),
      DEBT_POSITION_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String codIpaEnte,
    String gpdIupd,
    String codIud,
    ExtractionFilters filters,
    int limit,
    int offset
  ) {
    List<String> debtPositionTypeOrgCodes = filters != null ? filters.getDebtPositionTypeOrgCodes() : null;
    boolean debtPositionTypeOrgCodesEmpty = CollectionUtils.isEmpty(debtPositionTypeOrgCodes);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("codIpaEnte", codIpaEnte)
      .addValue("skipGpdIupdFilter", gpdIupd == null)
      .addValue("gpdIupd", gpdIupd)
      .addValue("skipCodIudFilter", codIud == null)
      .addValue("codIud", codIud)
      .addValue("skipUpdatedFromFilter", filters == null || filters.getModifiedFrom() == null)
      .addValue("updatedFrom", DateTimeUtils.toStartOfDay(filters != null ? filters.getModifiedFrom() : null))
      .addValue("skipUpdatedToExclusiveFilter", filters == null || filters.getModifiedTo() == null)
      .addValue("updatedToExclusive", DateTimeUtils.toStartOfNextDay(filters != null ? filters.getModifiedTo() : null))
      .addValue("debtPositionTypeOrgCodesEmpty", debtPositionTypeOrgCodesEmpty)
      .addValue("debtPositionTypeOrgCodes", debtPositionTypeOrgCodesEmpty ? Collections.singletonList(null) : debtPositionTypeOrgCodes);
  }
}
