package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT payments reporting";

  @Mock
  private NamedParameterJdbcTemplate fespJdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;
  @Mock
  private ResultSet resultSetMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(fespJdbcTemplateMock, sqlLoaderMock, resultSetMock);
  }

  @Test
  void givenDateRangeWhenFindThenQueryMyPayDatabase() {
    PaymentsReportingDao dao = buildDao();
    OffsetDateTime dateFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30),
      ZoneOffset.UTC
    );
    OffsetDateTime dateTo = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 11, 10, 30),
      ZoneOffset.UTC
    );
    List<String> expected = List.of("path.xml");

    when(fespJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && dateFrom.equals(params.getValue("dateFrom"))
          && Boolean.FALSE.equals(params.getValue("skipDateFromFilter"))
          && dateTo.equals(params.getValue("dateTo"))
          && Boolean.FALSE.equals(params.getValue("skipDateToFilter"))
          && Boolean.TRUE.equals(params.getValue("skipLogicalKeyFilter"))
          && params.getValue("logicalKey") == null
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      same(PaymentsReportingDao.PAYMENTS_REPORTING_FILE_ROW_MAPPER)
    )).thenReturn(expected);

    assertEquals(expected, dao.findByDateRange("IPA1", null, dateFrom, dateTo, 50, 100));
  }

  @Test
  void givenLastExtractionDateWhenFindThenQueryMyPayDatabase() {
    PaymentsReportingDao dao = buildDao();
    OffsetDateTime lastExtractionDate = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30),
      ZoneOffset.UTC
    );
    List<String> expected = List.of("path.xml");

    when(fespJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && lastExtractionDate.equals(params.getValue("dateFrom"))
          && Boolean.FALSE.equals(params.getValue("skipDateFromFilter"))
          && params.getValue("dateTo") == null
          && Boolean.TRUE.equals(params.getValue("skipDateToFilter"))
          && Boolean.TRUE.equals(params.getValue("skipLogicalKeyFilter"))
          && params.getValue("logicalKey") == null
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      same(PaymentsReportingDao.PAYMENTS_REPORTING_FILE_ROW_MAPPER)
    )).thenReturn(expected);

    assertEquals(expected, dao.findByDateRange("IPA1", lastExtractionDate, null, null, 50, 0));
  }

  @Test
  void givenLogicalKeyWhenFindThenQueryMyPayDatabase() {
    PaymentsReportingDao dao = buildDao();
    List<String> expected = List.of("path.xml");

    when(fespJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && params.getValue("dateFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipDateFromFilter"))
          && params.getValue("dateTo") == null
          && Boolean.TRUE.equals(params.getValue("skipDateToFilter"))
          && Boolean.FALSE.equals(params.getValue("skipLogicalKeyFilter"))
          && "FLOW-1".equals(params.getValue("logicalKey"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      same(PaymentsReportingDao.PAYMENTS_REPORTING_FILE_ROW_MAPPER)
    )).thenReturn(expected);

    assertEquals(expected, dao.findByLogicalKey("IPA1", "FLOW-1", 50, 0));
  }

  @Test
  void givenDatabaseRowWhenMappedThenExposePaymentsReportingFileName() throws Exception {
    when(resultSetMock.getString("de_nome_file_scaricato")).thenReturn("/mypay/reporting/FLOW-1.xml");

    String result = PaymentsReportingDao.PAYMENTS_REPORTING_FILE_ROW_MAPPER.mapRow(resultSetMock, 0);

    assertEquals("/mypay/reporting/FLOW-1.xml", result);
    verify(resultSetMock).getString("de_nome_file_scaricato");
  }

  @Test
  void givenPaymentsReportingSqlWhenLoadedThenIncludeSupportedFilters() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/fesp/payments-reporting/payments-reporting.sql"));

    assertTrue(sql.contains("FROM mygov_flusso_rend_spc rs"));
    assertTrue(sql.contains("rs.cod_ipa_ente = :ipaCode"));
    assertTrue(sql.contains("rs.cod_stato = 'OK'"));
    assertTrue(sql.contains("rs.dt_ultima_modifica >= :dateFrom"));
    assertTrue(sql.contains("rs.dt_ultima_modifica <= :dateTo"));
    assertTrue(sql.contains(":skipDateFromFilter = TRUE"));
    assertTrue(sql.contains(":skipDateToFilter = TRUE"));
    assertTrue(sql.contains(":skipLogicalKeyFilter = TRUE"));
    assertTrue(sql.contains("rs.cod_identificativo_flusso = :logicalKey"));
    assertTrue(sql.contains("ORDER BY rs.dt_creazione, rs.de_nome_file_scaricato"));
    assertTrue(sql.contains("LIMIT :limit"));
    assertTrue(sql.contains("OFFSET COALESCE(:offset, 0)"));
  }

  private PaymentsReportingDao buildDao() {
    when(sqlLoaderMock.load("fesp/payments-reporting/payments-reporting.sql")).thenReturn(FIND_BY_FILTERS_SQL);
    return new PaymentsReportingDao(fespJdbcTemplateMock, sqlLoaderMock);
  }
}
