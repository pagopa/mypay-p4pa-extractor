package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    String organizationId,
    String gpdIupd,
    String codIud,
    LocalDate updatedFrom,
    LocalDate updatedTo
  ) {
    return findDebtPositions(organizationId, gpdIupd, codIud, updatedFrom, updatedTo, Integer.MAX_VALUE, 0);
  }

  public List<DebtPosition> findDebtPositions(
    String organizationId,
    String gpdIupd,
    String codIud,
    LocalDate updatedFrom,
    LocalDate updatedTo,
    int limit,
    int offset
  ) {
    validateOrganizationId(organizationId);
    return mp4JdbcTemplate.query(
      findDebtPositionsSql,
      buildParams(organizationId, gpdIupd, codIud, updatedFrom, updatedTo, limit, offset),
      DEBT_POSITION_ROW_MAPPER
    );
  }

  public List<DebtPosition> findCancelledDebtPositions(
    String organizationId,
    String gpdIupd,
    String codIud,
    LocalDate updatedFrom,
    LocalDate updatedTo
  ) {
    return findCancelledDebtPositions(organizationId, gpdIupd, codIud, updatedFrom, updatedTo, Integer.MAX_VALUE, 0);
  }

  public List<DebtPosition> findCancelledDebtPositions(
    String organizationId,
    String gpdIupd,
    String codIud,
    LocalDate updatedFrom,
    LocalDate updatedTo,
    int limit,
    int offset
  ) {
    validateOrganizationId(organizationId);
    return mp4JdbcTemplate.query(
      findCancelledDebtPositionsSql,
      buildParams(organizationId, gpdIupd, codIud, updatedFrom, updatedTo, limit, offset),
      DEBT_POSITION_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String organizationId,
    String gpdIupd,
    String codIud,
    LocalDate updatedFrom,
    LocalDate updatedTo,
    int limit,
    int offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("organizationId", organizationId)
      .addValue("gpdIupd", gpdIupd)
      .addValue("codIud", codIud)
      .addValue("updatedFrom", DateTimeUtils.toStartOfDay(updatedFrom))
      .addValue("updatedToExclusive", DateTimeUtils.toStartOfNextDay(updatedTo));
  }

  private void validateOrganizationId(String organizationId) {
    if (organizationId == null || organizationId.isBlank()) {
      throw new IllegalArgumentException("organizationId must not be blank");
    }
  }
}
