package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
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
import java.util.Collections;
import java.util.List;

@Repository
public class AssessmentsDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypivot/assessments/assessments-export.sql";
  protected static final RowMapper<Assessments> ASSESSMENTS_ROW_MAPPER =
    DataClassRowMapper.newInstance(Assessments.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByFiltersSql;

  public AssessmentsDao(
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<Assessments> findByFilters(String ipaCode,
                                         OffsetDateTime lastExtractionDate,
                                         List<String> assessmentCodes,
                                         OffsetDateTime dateFrom,
                                         OffsetDateTime dateTo,
                                         int limit,
                                         int offset) {
    return mypivotJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, lastExtractionDate, assessmentCodes, dateFrom, dateTo, limit, offset),
      ASSESSMENTS_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String ipaCode,
                                            OffsetDateTime lastExtractionDate,
                                            List<String> assessmentCodes,
                                            OffsetDateTime dateFrom,
                                            OffsetDateTime dateTo,
                                            int limit,
                                            int offset) {
    boolean emptyAssessmentsCodes = CollectionUtils.isEmpty(assessmentCodes);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("lastExtractionDate", lastExtractionDate)
      .addValue("assessmentCodes", emptyAssessmentsCodes? Collections.singletonList(null) : assessmentCodes)
      .addValue("skipAssessmentCodesFilter", emptyAssessmentsCodes)
      .addValue("dateFrom", dateFrom)
      .addValue("dateTo", dateTo);
  }
}
