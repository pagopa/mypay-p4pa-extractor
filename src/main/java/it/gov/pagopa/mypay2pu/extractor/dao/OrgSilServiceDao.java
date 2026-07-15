package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrgSilServiceDao {

  private static final String FIND_PAID_NOTIFICATION_OUTCOME_SQL_PATH = "mypay/org-sil-service/paid-notification-outcome.sql";
  private static final String FIND_ACTUALIZATION_SQL_PATH = "mypay/org-sil-service/actualization.sql";
  protected static final RowMapper<OrgSilService> ORG_SIL_SERVICE_ROW_MAPPER =
    DataClassRowMapper.newInstance(OrgSilService.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final String findPaidNotificationOutcomeSql;
  private final String findActualizationSql;

  public OrgSilServiceDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.findPaidNotificationOutcomeSql = sqlLoader.load(FIND_PAID_NOTIFICATION_OUTCOME_SQL_PATH);
    this.findActualizationSql = sqlLoader.load(FIND_ACTUALIZATION_SQL_PATH);
  }

  public List<OrgSilService> findPaidNotificationOutcome(String codIpaEnte) {
    return findPaidNotificationOutcome(codIpaEnte, Integer.MAX_VALUE, 0);
  }

  public List<OrgSilService> findPaidNotificationOutcome(String codIpaEnte, int limit, int offset) {
    return mp4JdbcTemplate.query(
      findPaidNotificationOutcomeSql,
      buildParams(codIpaEnte, limit, offset),
      ORG_SIL_SERVICE_ROW_MAPPER
    );
  }

  public List<OrgSilService> findActualization(String codIpaEnte) {
    return findActualization(codIpaEnte, Integer.MAX_VALUE, 0);
  }

  public List<OrgSilService> findActualization(String codIpaEnte, int limit, int offset) {
    return mp4JdbcTemplate.query(
      findActualizationSql,
      buildParams(codIpaEnte, limit, offset),
      ORG_SIL_SERVICE_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(String codIpaEnte, int limit, int offset) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("codIpaEnte", codIpaEnte);
  }
}
