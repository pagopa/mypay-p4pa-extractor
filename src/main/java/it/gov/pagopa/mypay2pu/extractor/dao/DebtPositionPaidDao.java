package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
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
public class DebtPositionPaidDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/debt-positions-paid/debt-positions-paid.sql";
  protected static final RowMapper<DebtPositionPaid> DEBT_POSITION_PAID_ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPositionPaid.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final String findByFiltersSql;

  public DebtPositionPaidDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<DebtPositionPaid> findByFilters(
    String codIpaEnte,
    List<String> iuds,
    List<String> iuvs,
    OffsetDateTime createdFrom,
    OffsetDateTime createdTo,
    int limit,
    int offset
  ) {
    if (StringUtils.isEmpty(codIpaEnte)) {
      throw new IllegalArgumentException("codIpaEnte must not be blank");
    }
    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(codIpaEnte, iuds, iuvs, createdFrom, createdTo, limit, offset),
      DEBT_POSITION_PAID_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String codIpaEnte,
    List<String> iuds,
    List<String> iuvs,
    OffsetDateTime createdFrom,
    OffsetDateTime createdTo,
    int limit,
    int offset
  ) {
    boolean iudsEmpty = CollectionUtils.isEmpty(iuds);
    boolean iuvsEmpty = CollectionUtils.isEmpty(iuvs);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("codIpaEnte", codIpaEnte)
      .addValue("iudsEmpty", iudsEmpty)
      .addValue("iuds", iudsEmpty ? Collections.singletonList(null) : iuds)
      .addValue("iuvsEmpty", iuvsEmpty)
      .addValue("iuvs", iuvsEmpty ? Collections.singletonList(null) : iuvs)
      .addValue("skipCreatedFromFilter", createdFrom == null)
      .addValue("createdFrom", DateTimeUtils.toLocalDateTime(createdFrom))
      .addValue("skipCreatedToFilter", createdTo == null)
      .addValue("createdTo", DateTimeUtils.toLocalDateTime(createdTo));
  }
}
