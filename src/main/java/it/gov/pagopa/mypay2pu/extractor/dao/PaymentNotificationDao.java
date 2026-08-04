package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.PaymentNotification;
import it.gov.pagopa.mypay2pu.extractor.utils.QueryUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PaymentNotificationDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypay/payment-notification/payment-notification.sql";
  protected static final RowMapper<PaymentNotification> PAYMENT_NOTIFICATION_ROW_MAPPER =
    DataClassRowMapper.newInstance(PaymentNotification.class);

  private final NamedParameterJdbcTemplate mp4JdbcTemplate;
  private final String findByFiltersSql;

  public PaymentNotificationDao(
    @Qualifier("mp4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mp4JdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mp4JdbcTemplate = mp4JdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<PaymentNotification> findByFilters(
    String ipaCode,
    String iud,
    String iuv,
    LocalDateTime createdFrom,
    LocalDateTime createdTo,
    int limit,
    int offset
  ) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }

    return mp4JdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, iud, iuv, createdFrom, createdTo, limit, offset),
      PAYMENT_NOTIFICATION_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String ipaCode,
    String iud,
    String iuv,
    LocalDateTime createdFrom,
    LocalDateTime createdTo,
    int limit,
    int offset
  ) {
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("iud", iud)
      .addValue("iuv", iuv)
      .addValue("skipCreatedFromFilter", createdFrom == null)
      .addValue("createdFrom", createdFrom)
      .addValue("skipCreatedToFilter", createdTo == null)
      .addValue("createdTo", createdTo);
  }
}
