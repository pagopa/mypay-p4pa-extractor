package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import it.gov.pagopa.mypay2pu.extractor.validation.LogicalKeyPair;
import it.gov.pagopa.mypay2pu.extractor.validation.PairedLogicalKeyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
    String ipaCode,
    TreasuryCsvCompleteFilters filters,
    int limit,
    int offset
  ) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }
    if (mypivotJdbcTemplate == null) {
      throw new IllegalStateException("MyPivot datasource must be enabled for treasury extraction");
    }
    TreasuryCsvCompleteFilters effectiveFilters = filters != null
      ? filters
      : new TreasuryCsvCompleteFilters(null, null, null);
    LogicalKeyPair logicalKey = PairedLogicalKeyValidator.parseLogicalKey(effectiveFilters.logicalKey());
    return mypivotJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, logicalKey, effectiveFilters, limit, offset),
      TREASURY_CSV_COMPLETE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String ipaCode,
    LogicalKeyPair logicalKey,
    TreasuryCsvCompleteFilters filters,
    int limit,
    int offset
  ) {
    boolean skipBollettaLogicalKeyFilter = logicalKey.left().isEmpty() || logicalKey.right().isEmpty();
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("skipBollettaLogicalKeyFilter", skipBollettaLogicalKeyFilter)
      .addValue("bollettaLogicalKeys", skipBollettaLogicalKeyFilter
        ? java.util.Collections.singletonList(new Object[]{null, null})
        : QueryUtils.pairValues(logicalKey.left(), logicalKey.right()))
      .addValue("skipUpdatedFromFilter", filters.updatedFrom() == null)
      .addValue("updatedFrom", DateTimeUtils.toLocalDateTime(filters.updatedFrom()))
      .addValue("skipUpdatedToFilter", filters.updatedTo() == null)
      .addValue("updatedTo", DateTimeUtils.toLocalDateTime(filters.updatedTo()));
  }

  public record TreasuryCsvCompleteFilters(
    String logicalKey,
    OffsetDateTime updatedFrom,
    OffsetDateTime updatedTo
  ) {
  }
}
