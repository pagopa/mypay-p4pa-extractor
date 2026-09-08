package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.AssessmentsRegistry;
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
public class AssessmentsRegistryDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypivot/assessments-registry/assessments-registry-export.sql";
  protected static final RowMapper<AssessmentsRegistry> ASSESSMENTS_REGISTRY_ROW_MAPPER =
    DataClassRowMapper.newInstance(AssessmentsRegistry.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByFiltersSql;

  public AssessmentsRegistryDao(
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<AssessmentsRegistry> findByFilters(String ipaCode,
                                         OffsetDateTime lastExtractionDate,
                                         List<String> debtPositionTypeOrgCodes,
                                         OffsetDateTime dateFrom,
                                         OffsetDateTime dateTo,
                                         int limit,
                                         int offset) {
    return mypivotJdbcTemplate.query(
      findByFiltersSql,
      buildParams(
        ipaCode,
        debtPositionTypeOrgCodes,
        QueryUtils.resolveDateFrom(lastExtractionDate, dateFrom),
        dateTo,
        limit,
        offset
      ),
      ASSESSMENTS_REGISTRY_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String ipaCode,
                                            List<String> debtPositionTypeOrgCodes,
                                            OffsetDateTime dateFrom,
                                            OffsetDateTime dateTo,
                                            int limit,
                                            int offset) {
    boolean isEmptyCollection = CollectionUtils.isEmpty(debtPositionTypeOrgCodes);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("dateFrom", dateFrom)
      .addValue("skipDateFromFilter", dateFrom == null)
      .addValue("dateTo", dateTo)
      .addValue("skipDateToFilter", dateTo == null)
      .addValue("skipDebtPositionTypeOrgCodesFilter", isEmptyCollection)
      .addValue("debtPositionTypeOrgCodes", isEmptyCollection ? Collections.singletonList(null) : debtPositionTypeOrgCodes);
  }
}
