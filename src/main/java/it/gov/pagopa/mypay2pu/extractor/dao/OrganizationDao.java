package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
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

import java.util.List;

@Repository
public class OrganizationDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/organization/organization.sql";
  private static final String FIND_TREASURY_BY_IPA_SQL_PATH = "mypivot/organization/organization-pivot.sql";
  protected static final RowMapper<Organization> ORGANIZATION_ROW_MAPPER =
    DataClassRowMapper.newInstance(Organization.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final NamedParameterJdbcTemplate mpv4JdbcTemplate;
  private final String findByFiltersSql;
  private final String findTreasuryByIpaSql;

  public OrganizationDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mpv4JdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.mpv4JdbcTemplate = mpv4JdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
    this.findTreasuryByIpaSql = sqlLoader.load(FIND_TREASURY_BY_IPA_SQL_PATH);
  }

  public List<Organization> findByFilters(List<String> ipaCodes, ExtractionFilters filters) {
    return mp4JdbcTemplate.query(findByFiltersSql, buildFiltersParams(ipaCodes, filters, null, null), ORGANIZATION_ROW_MAPPER);
  }

  public List<Organization> findByFilters(List<String> ipaCodes, ExtractionFilters filters, int limit, int offset) {
    MapSqlParameterSource params = buildFiltersParams(ipaCodes, filters, limit, offset);
    return mp4JdbcTemplate.query(findByFiltersSql, params, ORGANIZATION_ROW_MAPPER);
  }

  public boolean isTreasuryEnabled(String ipaCode) {
    if (mpv4JdbcTemplate == null || ipaCode == null) {
      return false;
    }
    Boolean exists = mpv4JdbcTemplate.queryForObject(findTreasuryByIpaSql, buildTreasuryParams(ipaCode), Boolean.class);
    return Boolean.TRUE.equals(exists);
  }

  private MapSqlParameterSource buildFiltersParams(
    List<String> ipaCodes,
    ExtractionFilters filters,
    Integer limit,
    Integer offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCodes", ipaCodes)
      .addValue("modifiedFrom", DateTimeUtils.toStartOfDay(filters != null ? filters.getModifiedFrom() : null))
      .addValue("modifiedToExclusive", DateTimeUtils.toStartOfNextDay(filters != null ? filters.getModifiedTo() : null));
  }

  private MapSqlParameterSource buildTreasuryParams(String ipaCode) {
    return new MapSqlParameterSource()
      .addValue("codIpaEnte", ipaCode);
  }
}
