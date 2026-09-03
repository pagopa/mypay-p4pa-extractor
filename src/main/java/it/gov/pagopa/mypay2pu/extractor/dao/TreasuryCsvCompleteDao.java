package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
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

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class TreasuryCsvCompleteDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypivot/treasury-csv-complete/treasury-csv-complete.sql";
  protected static final RowMapper<TreasuryCsvComplete> TREASURY_CSV_COMPLETE_ROW_MAPPER =
    DataClassRowMapper.newInstance(TreasuryCsvComplete.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByFiltersSql;

  public TreasuryCsvCompleteDao(
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<TreasuryCsvComplete> findByFilters(
    List<String> ipaCodes,
    TreasuryCsvCompleteFilters filters,
    int limit,
    int offset
  ) {
    if (CollectionUtils.isEmpty(ipaCodes)) {
      throw new IllegalArgumentException("ipaCodes must not be empty");
    }
    if (mypivotJdbcTemplate == null) {
      throw new IllegalStateException("MyPivot datasource must be enabled for treasury extraction");
    }
    TreasuryCsvCompleteFilters effectiveFilters = filters != null
      ? filters
      : new TreasuryCsvCompleteFilters(null, null, null, null, null);
    TreasuryLogicalKey treasuryLogicalKey = parseLogicalKey(effectiveFilters.logicalKey());
    return mypivotJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCodes, treasuryLogicalKey, effectiveFilters, limit, offset),
      TREASURY_CSV_COMPLETE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    List<String> ipaCodes,
    TreasuryLogicalKey logicalKey,
    TreasuryCsvCompleteFilters filters,
    int limit,
    int offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCodes", ipaCodes)
      .addValue("annoBolletta", logicalKey.annoBolletta())
      .addValue("codBolletta", logicalKey.codBolletta())
      .addValue("createdFrom", DateTimeUtils.toLocalDateTime(filters.createdFrom()))
      .addValue("createdTo", DateTimeUtils.toLocalDateTime(filters.createdTo()))
      .addValue("updatedFrom", DateTimeUtils.toLocalDateTime(filters.updatedFrom()))
      .addValue("updatedTo", DateTimeUtils.toLocalDateTime(filters.updatedTo()));
  }

  private TreasuryLogicalKey parseLogicalKey(String logicalKey) {
    if (logicalKey == null) {
      return new TreasuryLogicalKey(null, null);
    }

    String[] parts = logicalKey.split("\\|", -1);
    if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
      throw new IllegalArgumentException("logicalKey must be in the format <annoBolletta>|<codBolletta>");
    }
    return new TreasuryLogicalKey(parts[0], parts[1]);
  }

  private record TreasuryLogicalKey(String annoBolletta, String codBolletta) {
  }

  public record TreasuryCsvCompleteFilters(
    String logicalKey,
    OffsetDateTime createdFrom,
    OffsetDateTime createdTo,
    OffsetDateTime updatedFrom,
    OffsetDateTime updatedTo
  ) {
  }
}
