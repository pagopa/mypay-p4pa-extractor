package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.PaymentNotification;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT payment notifications";

  @Mock
  private NamedParameterJdbcTemplate mypivotJdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mypivotJdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenFiltersWhenFindThenQueryPagedMyPivotDatabase() {
    PaymentNotificationDao dao = buildDao();
    String ipaCode = "IPA1";
    String iud = "IUD-1";
    String iuv = "IUV-1";
    LocalDateTime createdFrom = LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30);
    LocalDateTime createdTo = LocalDateTime.of(2026, Month.JANUARY, 11, 10, 30);
    List<PaymentNotification> expected = List.of(buildPaymentNotification());

    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        ipaCode.equals(params.getValue("ipaCode"))
          && iud.equals(params.getValue("iud"))
          && iuv.equals(params.getValue("iuv"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedFromFilter"))
          && createdFrom.equals(params.getValue("createdFrom"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedToFilter"))
          && createdTo.equals(params.getValue("createdTo"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      same(PaymentNotificationDao.PAYMENT_NOTIFICATION_ROW_MAPPER)
    )).thenReturn(expected);

    List<PaymentNotification> result = dao.findByFilters(
      ipaCode, iud, iuv, createdFrom, createdTo, 50, 100
    );

    assertEquals(expected, result);
  }

  @Test
  void givenNoOptionalFiltersWhenFindThenSkipTheirFilters() {
    PaymentNotificationDao dao = buildDao();

    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && params.hasValue("iud")
          && params.getValue("iud") == null
          && params.hasValue("iuv")
          && params.getValue("iuv") == null
          && Boolean.TRUE.equals(params.getValue("skipCreatedFromFilter"))
          && params.hasValue("createdFrom")
          && params.getValue("createdFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipCreatedToFilter"))
          && params.hasValue("createdTo")
          && params.getValue("createdTo") == null
          && Integer.valueOf(10).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      same(PaymentNotificationDao.PAYMENT_NOTIFICATION_ROW_MAPPER)
    )).thenReturn(List.of());

    List<PaymentNotification> result = dao.findByFilters(
      "IPA1", null, null, null, null, 10, 0
    );

    assertEquals(List.of(), result);
  }

  @Test
  void givenInvalidPagingWhenFindThenRejectBeforeDatabaseInteraction() {
    PaymentNotificationDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters("IPA1", null, null, null, null, 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenBlankIpaCodeWhenFindThenRejectBeforeDatabaseInteraction() {
    PaymentNotificationDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters(" ", null, null, null, null, 10, 0)
    );

    assertEquals("ipaCode must not be blank", exception.getMessage());
  }

  @Test
  void givenMyPivotDisabledWhenFindThenRejectBeforeDatabaseInteraction() {
    PaymentNotificationDao dao = buildDao(null);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> dao.findByFilters("IPA1", null, null, null, null, 10, 0)
    );

    assertEquals("MyPivot datasource must be enabled for payment notification extraction", exception.getMessage());
  }

  @Test
  void givenPaymentNotificationSqlWhenLoadedThenUsesIpaCodeFilterAndCreatedOrdering() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/mypivot/payment-notification/payment-notification.sql"));

    assertTrue(sql.contains("e.cod_ipa_ente = :ipaCode"));
    assertTrue(sql.contains("ORDER BY fi.dt_creazione"));
  }

  private PaymentNotificationDao buildDao() {
    return buildDao(mypivotJdbcTemplateMock);
  }

  private PaymentNotificationDao buildDao(NamedParameterJdbcTemplate mypivotJdbcTemplate) {
    when(sqlLoaderMock.load("mypivot/payment-notification/payment-notification.sql"))
      .thenReturn(FIND_BY_FILTERS_SQL);
    return new PaymentNotificationDao(mypivotJdbcTemplate, sqlLoaderMock);
  }

  private PaymentNotification buildPaymentNotification() {
    return new PaymentNotification(
      "IUD-1",
      "IUV-1",
      "F",
      "RSSMRA80A01H501U",
      "Mario Rossi",
      "Via Roma",
      "10",
      "00100",
      "Roma",
      "RM",
      "IT",
      "mario.rossi@example.com",
      LocalDate.of(2026, Month.JANUARY, 10),
      BigDecimal.TEN,
      BigDecimal.ONE,
      "TAX",
      "BBT",
      "Pagamento tassa",
      "9/0101101IM/",
      "bilancio",
      "IPA1",
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30)
    );
  }
}
