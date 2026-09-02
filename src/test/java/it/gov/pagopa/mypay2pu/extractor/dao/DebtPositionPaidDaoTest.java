package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionPaidDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT paid debt positions";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenAllFiltersWhenFindByFiltersThenQueryPagedMp4Database() {
    DebtPositionPaidDao dao = buildDao();
    OffsetDateTime createdFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30),
      ZoneOffset.ofHours(1)
    );
    OffsetDateTime createdTo = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 11, 10, 30),
      ZoneOffset.ofHours(1)
    );

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.FALSE.equals(params.getValue("iudsEmpty"))
          && List.of("IUD-1").equals(params.getValue("iuds"))
          && Boolean.FALSE.equals(params.getValue("iuvsEmpty"))
          && List.of("IUV-1").equals(params.getValue("iuvs"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedFromFilter"))
          && createdFrom.toLocalDateTime().equals(params.getValue("createdFrom"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedToFilter"))
          && createdTo.toLocalDateTime().equals(params.getValue("createdTo"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionPaidDao.DEBT_POSITION_PAID_ROW_MAPPER)
    )).thenReturn(List.of());

    List<DebtPositionPaid> result = dao.findByFilters(
      "IPA1", List.of("IUD-1"), List.of("IUV-1"), createdFrom, createdTo, 50, 100
    );

    assertEquals(List.of(), result);
  }

  @Test
  void givenCreatedFromWhenFindByFiltersThenApplyOnlyLowerBound() {
    DebtPositionPaidDao dao = buildDao();
    OffsetDateTime createdFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 0, 0),
      ZoneOffset.ofHours(1)
    );

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        Boolean.FALSE.equals(params.getValue("skipCreatedFromFilter"))
          && createdFrom.toLocalDateTime().equals(params.getValue("createdFrom"))
          && Boolean.TRUE.equals(params.getValue("skipCreatedToFilter"))
          && params.getValue("createdTo") == null
      ),
      same(DebtPositionPaidDao.DEBT_POSITION_PAID_ROW_MAPPER)
    )).thenReturn(List.of());

    assertEquals(List.of(), dao.findByFilters("IPA1", List.of(), List.of(), createdFrom, null, 10, 0));
  }

  @Test
  void givenCreatedToWhenFindByFiltersThenApplyOnlyUpperBound() {
    DebtPositionPaidDao dao = buildDao();
    OffsetDateTime createdTo = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 11, 23, 59),
      ZoneOffset.ofHours(1)
    );

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        Boolean.TRUE.equals(params.getValue("skipCreatedFromFilter"))
          && params.getValue("createdFrom") == null
          && Boolean.FALSE.equals(params.getValue("skipCreatedToFilter"))
          && createdTo.toLocalDateTime().equals(params.getValue("createdTo"))
      ),
      same(DebtPositionPaidDao.DEBT_POSITION_PAID_ROW_MAPPER)
    )).thenReturn(List.of());

    assertEquals(List.of(), dao.findByFilters("IPA1", List.of(), List.of(), null, createdTo, 10, 0));
  }

  @Test
  void givenOnlyIpaCodeWhenFindByFiltersThenQueryFullExtraction() {
    DebtPositionPaidDao dao = buildDao();

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.TRUE.equals(params.getValue("iudsEmpty"))
          && Collections.singletonList(null).equals(params.getValue("iuds"))
          && Boolean.TRUE.equals(params.getValue("iuvsEmpty"))
          && Collections.singletonList(null).equals(params.getValue("iuvs"))
          && Boolean.TRUE.equals(params.getValue("skipCreatedFromFilter"))
          && params.getValue("createdFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipCreatedToFilter"))
          && params.getValue("createdTo") == null
          && Integer.valueOf(10).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionPaidDao.DEBT_POSITION_PAID_ROW_MAPPER)
    )).thenReturn(List.of());

    assertEquals(List.of(), dao.findByFilters("IPA1", List.of(), List.of(), null, null, 10, 0));
  }

  @Test
  void givenPaidDebtPositionsSqlWhenLoadedThenIncludesReceiptMappingAndBusinessFilters() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/mypay/debt-positions-paid/debt-positions-paid.sql"));

    assertTrue(sql.contains("e.cod_ipa_ente = :codIpaEnte"));
    assertTrue(sql.contains("r.fiscal_code AS cod_fiscale_pa1"));
    assertTrue(sql.contains("r.company_name AS de_nome_pa1"));
    assertTrue(sql.contains("r.transfer_category_1 AS cod_tassonomico_dovuto_pa1"));
    assertTrue(sql.contains("de_status.cod_stato = 'COMPLETATO'"));
    assertTrue(sql.contains("flow_status.cod_stato = 'CARICATO'"));
    assertTrue(sql.contains("de.cod_iud IN (:iuds)"));
    assertTrue(sql.contains("de.cod_rp_silinviarp_id_univoco_versamento IN (:iuvs)"));
    assertTrue(sql.contains("de.dt_creazione >= :createdFrom"));
    assertTrue(sql.contains("de.dt_creazione <= :createdTo"));
  }

  private DebtPositionPaidDao buildDao() {
    when(sqlLoaderMock.load("mypay/debt-positions-paid/debt-positions-paid.sql")).thenReturn(FIND_BY_FILTERS_SQL);
    return new DebtPositionPaidDao(mp4JdbcTemplateMock, sqlLoaderMock);
  }
}
