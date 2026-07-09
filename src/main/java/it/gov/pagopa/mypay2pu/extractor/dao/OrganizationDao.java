package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.OrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrganizationDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/organization/organization.sql";
  private static final String FIND_TREASURY_BY_IPA_SQL_PATH = "mypivot/organization/organization-pivot.sql";
  private static final RowMapper<OrganizationDTO> ORGANIZATION_ROW_MAPPER =
    DataClassRowMapper.newInstance(OrganizationDTO.class);

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

  public List<OrganizationDTO> findByFilters(String ipaCode, ExtractionFilters filters) {
    return mp4JdbcTemplate.query(findByFiltersSql, buildFiltersParams(ipaCode, filters, null, null), ORGANIZATION_ROW_MAPPER);
  }

  public List<OrganizationDTO> findByFilters(String ipaCode, ExtractionFilters filters, int limit, int offset) {
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be greater than 0");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be non-negative");
    }
    MapSqlParameterSource params = buildFiltersParams(ipaCode, filters, limit, offset);
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
    String ipaCode,
    ExtractionFilters filters,
    Integer limit,
    Integer offset
  ) {
    return new MapSqlParameterSource()
      .addValue("ipaCode", ipaCode)
      .addValue("modifiedFrom", toStartOfDay(filters != null ? filters.getModifiedFrom() : null))
      .addValue("modifiedToExclusive", toStartOfNextDay(filters != null ? filters.getModifiedTo() : null))
      .addValue("limit", limit)
      .addValue("offset", offset);
  }

  private MapSqlParameterSource buildTreasuryParams(String ipaCode) {
    return new MapSqlParameterSource()
      .addValue("codIpaEnte", ipaCode);
  }

  private LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  private LocalDateTime toStartOfNextDay(LocalDate date) {
    return date == null ? null : date.plusDays(1).atStartOfDay();
  }
}
