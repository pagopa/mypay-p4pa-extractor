package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AssessmentsDao {

  private static final String FIND_BY_LAST_MIGRATION_SQL_PATH = "mypivot/assessments/assessments-export-by-last-migration.sql";
  private static final String FIND_BY_DATE_RANGE_SQL_PATH = "mypivot/assessments/assessments-export-by-date-range.sql";
  protected static final RowMapper<Assessments> ASSESSMENTS_ROW_MAPPER =
    DataClassRowMapper.newInstance(Assessments.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByLastMigrationSql;
  private final String findByDateRangeSql;

  public AssessmentsDao(
    @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByLastMigrationSql = sqlLoader.load(FIND_BY_LAST_MIGRATION_SQL_PATH);
    this.findByDateRangeSql = sqlLoader.load(FIND_BY_DATE_RANGE_SQL_PATH);
  }

  public List<Assessments> findByFilters(String codIpaEnte, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    return mypivotJdbcTemplate.query(
      dateFrom != null && dateTo != null ? findByDateRangeSql : findByLastMigrationSql,
      buildParams(codIpaEnte, dateFrom, dateTo),
      ASSESSMENTS_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String codIpaEnte, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("codIpaEnte", codIpaEnte);
    if (dateFrom != null && dateTo != null) {
      params.addValue("dateFrom", dateFrom)
        .addValue("dateTo", dateTo);
    } else {
      params.addValue("dataUltimaMigrazione", dateFrom);
    }
    return params;
  }
}
