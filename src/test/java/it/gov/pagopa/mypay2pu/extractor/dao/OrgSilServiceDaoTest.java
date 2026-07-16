package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgSilServiceDaoTest {

  private static final String FIND_PAID_NOTIFICATION_OUTCOME_SQL = "SELECT paid";
  private static final String FIND_ACTUALIZATION_SQL = "SELECT actualization";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenCodIpaEnteWhenFindPaidNotificationOutcomeThenQueryMp4Database() {
    OrgSilServiceDao dao = buildDao();
    List<OrgSilService> expected = List.of(new OrgSilService(
      "IPA1",
      "app",
      "serviceType",
      "serviceUrl",
      true,
      "kid",
      "subject",
      "issuer",
      "HS512",
      "signingKey",
      "basicAuthUrl",
      "basicUser",
      "basicPsw"
    ));

    when(mp4JdbcTemplateMock.query(
      eq(FIND_PAID_NOTIFICATION_OUTCOME_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Integer.valueOf(Integer.MAX_VALUE).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(OrgSilServiceDao.ORG_SIL_SERVICE_ROW_MAPPER)
    )).thenReturn(expected);

    List<OrgSilService> result = dao.findPaidNotificationOutcome("IPA1");

    assertEquals(expected, result);
  }

  @Test
  void givenPagedParamsWhenFindPaidNotificationOutcomeThenUseLimitAndOffset() {
    OrgSilServiceDao dao = buildDao();

    when(mp4JdbcTemplateMock.query(
      eq(FIND_PAID_NOTIFICATION_OUTCOME_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(OrgSilServiceDao.ORG_SIL_SERVICE_ROW_MAPPER)
    )).thenReturn(List.of());

    List<OrgSilService> result = dao.findPaidNotificationOutcome("IPA1", 50, 100);

    assertEquals(List.of(), result);
  }

  @Test
  void givenCodIpaEnteWhenFindActualizationThenQueryMp4Database() {
    OrgSilServiceDao dao = buildDao();
    List<OrgSilService> expected = List.of(new OrgSilService(
      "IPA2",
      "app",
      "serviceType",
      "serviceUrl",
      true,
      "kid",
      "subject",
      "issuer",
      "HS512",
      "signingKey",
      "basicAuthUrl",
      "basicUser",
      "basicPsw"
    ));

    when(mp4JdbcTemplateMock.query(
      eq(FIND_ACTUALIZATION_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA2".equals(params.getValue("codIpaEnte"))
          && Integer.valueOf(Integer.MAX_VALUE).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(OrgSilServiceDao.ORG_SIL_SERVICE_ROW_MAPPER)
    )).thenReturn(expected);

    List<OrgSilService> result = dao.findActualization("IPA2");

    assertEquals(expected, result);
  }

  @Test
  void givenPagedParamsWhenFindActualizationThenUseLimitAndOffset() {
    OrgSilServiceDao dao = buildDao();

    when(mp4JdbcTemplateMock.query(
      eq(FIND_ACTUALIZATION_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA2".equals(params.getValue("codIpaEnte"))
          && Integer.valueOf(25).equals(params.getValue("limit"))
          && Integer.valueOf(10).equals(params.getValue("offset"))
      ),
      same(OrgSilServiceDao.ORG_SIL_SERVICE_ROW_MAPPER)
    )).thenReturn(List.of());

    List<OrgSilService> result = dao.findActualization("IPA2", 25, 10);

    assertEquals(List.of(), result);
  }

  @Test
  void givenInvalidLimitWhenFindPaidNotificationOutcomeThenThrowIllegalArgumentException() {
    OrgSilServiceDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findPaidNotificationOutcome("IPA1", 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  private OrgSilServiceDao buildDao() {
    when(sqlLoaderMock.load("mypay/org-sil-service/paid-notification-outcome.sql")).thenReturn(FIND_PAID_NOTIFICATION_OUTCOME_SQL);
    when(sqlLoaderMock.load("mypay/org-sil-service/actualization.sql")).thenReturn(FIND_ACTUALIZATION_SQL);
    return new OrgSilServiceDao(mp4JdbcTemplateMock, sqlLoaderMock);
  }
}
