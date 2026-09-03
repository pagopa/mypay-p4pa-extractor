package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentNotification;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentNotificationDao {

  private static final String FIND_BY_FILTERS_SQL_PATH = "mypivot/payment-notification/payment-notification.sql";
  protected static final RowMapper<PaymentNotification> PAYMENT_NOTIFICATION_ROW_MAPPER =
    DataClassRowMapper.newInstance(PaymentNotification.class);

  private final NamedParameterJdbcTemplate mypivotJdbcTemplate;
  private final String findByFiltersSql;

  public PaymentNotificationDao(
    @Autowired(required = false) @Qualifier("mpv4NamedParameterJdbcTemplate") NamedParameterJdbcTemplate mypivotJdbcTemplate,
    SqlLoader sqlLoader
  ) {
    this.mypivotJdbcTemplate = mypivotJdbcTemplate;
    this.findByFiltersSql = sqlLoader.load(FIND_BY_FILTERS_SQL_PATH);
  }

  public List<PaymentNotification> findByFilters(
    String ipaCode,
    List<String> iuds,
    List<String> iuvs,
    LocalDateTime createdFrom,
    LocalDateTime createdTo,
    int limit,
    int offset
  ) {
    if (!StringUtils.hasText(ipaCode)) {
      throw new IllegalArgumentException("ipaCode must not be blank");
    }
    if (mypivotJdbcTemplate == null) {
      throw new IllegalStateException("MyPivot datasource must be enabled for payment notification extraction");
    }

    return mypivotJdbcTemplate.query(
      findByFiltersSql,
      buildParams(ipaCode, iuds, iuvs, createdFrom, createdTo, limit, offset),
      PAYMENT_NOTIFICATION_ROW_MAPPER
    );
  }

  private MapSqlParameterSource buildParams(
    String ipaCode,
    List<String> iuds,
    List<String> iuvs,
    LocalDateTime createdFrom,
    LocalDateTime createdTo,
    int limit,
    int offset
  ) {
    boolean iudsEmpty = CollectionUtils.isEmpty(iuds);
    boolean iuvsEmpty = CollectionUtils.isEmpty(iuvs);
    return QueryUtils.buildPaginatedFilterParams(limit, offset)
      .addValue("ipaCode", ipaCode)
      .addValue("iudsEmpty", iudsEmpty)
      .addValue("iuds", iudsEmpty ? Collections.singletonList(null) : iuds)
      .addValue("iuvsEmpty", iuvsEmpty)
      .addValue("iuvs", iuvsEmpty ? Collections.singletonList(null) : iuvs)
      .addValue("skipCreatedFromFilter", createdFrom == null)
      .addValue("createdFrom", createdFrom)
      .addValue("skipCreatedToFilter", createdTo == null)
      .addValue("createdTo", createdTo);
  }
}
