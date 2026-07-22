package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DebtPositionTypeDao {

  private static final String FIND_ALL_SQL_PATH = "mypay/debt-position-type/debt-position-type.sql";
  private static final String NOTIFICA_IO_MARKDOWN_PROPERTY = "notificaIo.markdown";
  private static final String NOTIFICA_IO_SUBJECT_PROPERTY = "notificaIo.subject";
  protected static final RowMapper<DebtPositionType> DEBT_POSITION_TYPE_ROW_MAPPER =
    DataClassRowMapper.newInstance(DebtPositionType.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final ExtractorExportProperties exportProperties;
  private final MyPayProperties myPayProperties;
  private final String findAllSql;

  public DebtPositionTypeDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    ExtractorExportProperties exportProperties,
    MyPayProperties myPayProperties,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.exportProperties = exportProperties;
    this.myPayProperties = myPayProperties;
    this.findAllSql = sqlLoader.load(FIND_ALL_SQL_PATH);
  }

  public List<DebtPositionType> findAll() {
    return mp4JdbcTemplate.query(findAllSql, buildParams(), DEBT_POSITION_TYPE_ROW_MAPPER);
  }

  private MapSqlParameterSource buildParams() {
    return new MapSqlParameterSource()
      .addValue("brokerCf", exportProperties.brokerCf())
      .addValue("ioTemplateMessage", myPayProperties.globalProperties().get(NOTIFICA_IO_MARKDOWN_PROPERTY))
      .addValue("ioTemplateSubject", myPayProperties.globalProperties().get(NOTIFICA_IO_SUBJECT_PROPERTY));
  }
}
