package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
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

  public List<DebtPosition> findDebtPositions(String codIpaEnte,
                                              List<String> iuvs,
                                              OffsetDateTime dateFrom,
                                              OffsetDateTime dateTo,
                                              int limit,
                                              int offset) {
    return findByFilters(findDebtPositionsSql, codIpaEnte, iuvs, dateFrom, dateTo, limit, offset);
  }

  public List<DebtPosition> findCancelledDebtPositions(String codIpaEnte,
                                                       List<String> iuvs,
                                                       OffsetDateTime dateFrom,
                                                       OffsetDateTime dateTo,
                                                       int limit,
                                                       int offset) {
    return findByFilters(findCancelledDebtPositionsSql, codIpaEnte, iuvs, dateFrom, dateTo, limit, offset);
  }

  private List<DebtPosition> findByFilters(String sql,
                                           String codIpaEnte,
                                           List<String> iuvs,
                                           OffsetDateTime dateFrom,
                                           OffsetDateTime dateTo,
                                           int limit,
                                           int offset) {
    if (StringUtils.isEmpty(codIpaEnte)) {
      throw new IllegalArgumentException("codIpaEnte must not be blank");
    }
    return mp4JdbcTemplate.query(
      sql,
      buildParams(codIpaEnte, iuvs, dateFrom, dateTo, limit, offset),
      DEBT_POSITION_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String codIpaEnte,
                                            List<String> iuvs,
                                            OffsetDateTime dateFrom,
                                            OffsetDateTime dateTo,
                                            int limit,
                                            int offset) {
    boolean iuvsEmpty = CollectionUtils.isEmpty(iuvs);

    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("codIpaEnte", codIpaEnte)
      .addValue("skipCodIuvFilter", iuvsEmpty)
      .addValue("iuvs", iuvsEmpty ? Collections.singletonList(null) : iuvs)
      .addValue("skipDateFromFilter", dateFrom == null)
      .addValue("dateFrom", DateTimeUtils.toLocalDateTime(dateFrom))
      .addValue("skipDateToExclusiveFilter", dateTo == null)
      .addValue("dateToExclusive", DateTimeUtils.toLocalDateTime(dateTo));
  }
}
