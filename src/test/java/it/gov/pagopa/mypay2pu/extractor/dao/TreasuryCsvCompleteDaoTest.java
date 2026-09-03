package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreasuryCsvCompleteDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT treasury flows";

  @Mock
  private NamedParameterJdbcTemplate mypivotJdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(mypivotJdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenNoOptionalFiltersWhenFindByFiltersThenQueryCompleteExport() {
    TreasuryCsvCompleteDao dao = buildDao();
    mockQuery(params ->
      List.of("IPA1").equals(params.getValue("ipaCodes"))
        && params.getValue("annoBolletta") == null
        && params.getValue("codBolletta") == null
        && params.getValue("createdFrom") == null
        && params.getValue("createdTo") == null
        && params.getValue("updatedFrom") == null
        && params.getValue("updatedTo") == null
        && Integer.valueOf(50).equals(params.getValue("limit"))
        && Integer.valueOf(0).equals(params.getValue("offset"))
    );

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1"), null, 50, 0));
  }

  @Test
  void givenMultipleIpaCodesWhenFindByFiltersThenApplyMultiOrganizationFilter() {
    TreasuryCsvCompleteDao dao = buildDao();
    mockQuery(params -> List.of("IPA1", "IPA2").equals(params.getValue("ipaCodes")));

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1", "IPA2"), null, 50, 0));
  }

  @Test
  void givenLogicalKeyWhenFindByFiltersThenApplyBollettaKeyComponents() {
    TreasuryCsvCompleteDao dao = buildDao();
    mockQuery(params ->
      "2026".equals(params.getValue("annoBolletta"))
        && "BOLLETTA-1".equals(params.getValue("codBolletta"))
    );

    assertEquals(List.of(), dao.findByFilters(
      List.of("IPA1"),
      filters("2026|BOLLETTA-1", null, null, null, null),
      50,
      0
    ));
  }

  @Test
  void givenCreatedFromWhenFindByFiltersThenApplyCreationLowerBound() {
    TreasuryCsvCompleteDao dao = buildDao();
    OffsetDateTime createdFrom = dateTime(10);
    mockQuery(params ->
      createdFrom.toLocalDateTime().equals(params.getValue("createdFrom"))
        && params.getValue("createdTo") == null
    );

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1"), filters(null, createdFrom, null, null, null), 50, 0));
  }

  @Test
  void givenCreatedToWhenFindByFiltersThenApplyCreationUpperBound() {
    TreasuryCsvCompleteDao dao = buildDao();
    OffsetDateTime createdTo = dateTime(11);
    mockQuery(params ->
      params.getValue("createdFrom") == null
        && createdTo.toLocalDateTime().equals(params.getValue("createdTo"))
    );

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1"), filters(null, null, createdTo, null, null), 50, 0));
  }

  @Test
  void givenUpdatedFromWhenFindByFiltersThenApplyModificationLowerBound() {
    TreasuryCsvCompleteDao dao = buildDao();
    OffsetDateTime updatedFrom = dateTime(12);
    mockQuery(params ->
      updatedFrom.toLocalDateTime().equals(params.getValue("updatedFrom"))
        && params.getValue("updatedTo") == null
    );

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1"), filters(null, null, null, updatedFrom, null), 50, 0));
  }

  @Test
  void givenUpdatedToWhenFindByFiltersThenApplyModificationUpperBound() {
    TreasuryCsvCompleteDao dao = buildDao();
    OffsetDateTime updatedTo = dateTime(13);
    mockQuery(params ->
      params.getValue("updatedFrom") == null
        && updatedTo.toLocalDateTime().equals(params.getValue("updatedTo"))
    );

    assertEquals(List.of(), dao.findByFilters(List.of("IPA1"), filters(null, null, null, null, updatedTo), 50, 0));
  }

  @Test
  void givenDisabledMyPivotDatasourceWhenFindByFiltersThenThrowIllegalStateException() {
    TreasuryCsvCompleteDao dao = buildDao(null);
    List<String> ipaCodes = List.of("IPA1");

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> dao.findByFilters(ipaCodes, null, 50, 0)
    );

    assertEquals("MyPivot datasource must be enabled for treasury extraction", exception.getMessage());
  }

  @Test
  void givenTreasurySqlWhenLoadedThenIncludeAllMandatoryFiltersAndNullColumns() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/mypivot/treasury-csv-complete/treasury-csv-complete.sql"));

    assertTrue(sql.contains("FROM mygov_flusso_tesoreria ft"));
    assertTrue(sql.contains("JOIN mygov_ente e"));
    assertTrue(sql.contains("ON ft.mygov_ente_id = e.mygov_ente_id"));
    assertTrue(sql.contains("NULL AS cod_ente_bt"));
    assertTrue(sql.contains("NULL AS cod_istat_ente"));
    assertTrue(sql.contains("e.cod_ipa_ente IN (:ipaCodes)"));
    assertTrue(sql.contains("ft.de_anno_bolletta = :annoBolletta"));
    assertTrue(sql.contains("ft.cod_bolletta = :codBolletta"));
    assertTrue(sql.contains("ft.dt_creazione >= :createdFrom"));
    assertTrue(sql.contains("ft.dt_creazione <= :createdTo"));
    assertTrue(sql.contains("ft.dt_ultima_modifica >= :updatedFrom"));
    assertTrue(sql.contains("ft.dt_ultima_modifica <= :updatedTo"));
    assertTrue(sql.contains("ORDER BY ft.de_anno_bolletta, ft.cod_bolletta"));
  }

  @Test
  void givenTreasuryProjectionWhenCreatedThenExposeAllMappedFieldsAndLogicalKey() {
    TreasuryCsvComplete treasury = new TreasuryCsvComplete(
      "2026", "BOLLETTA-1", null, null, "IPA1", "IUF", "IUV", "CONTO", "DOMINIO", "MOVIMENTO",
      "CAUSALE", "Causale", "IP", LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 1), "2026", "DOCUMENTO", "BOLLO", "Cognome",
      "Nome", "Via", "00100", "Roma", "CF", "PIVA", "ABI", "CAB", "IBAN", "CONTO-ANAGRAFICA",
      "Provvisorio", "COD-PROVVISORIO", "TIPO-CONTO", "PROCESSO", "PG-ESECUZIONE", "PG-TRASFERIMENTO",
      "PG-PROCESSO", LocalDateTime.of(2026, Month.JANUARY, 10, 10, 2), true,
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 3), "GESTIONALE", "END-TO-END"
    );

    TestUtils.checkNotNullFields(treasury, "codEnteBt", "codIstatEnte");
    assertEquals("2026|BOLLETTA-1", treasury.logicalKey());
    assertNull(treasury.codEnteBt());
    assertNull(treasury.codIstatEnte());
  }

  private void mockQuery(ArgumentMatcher<MapSqlParameterSource> paramsMatcher) {
    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.argThat(paramsMatcher),
      same(TreasuryCsvCompleteDao.TREASURY_CSV_COMPLETE_ROW_MAPPER)
    )).thenReturn(List.of());
  }

  private TreasuryCsvCompleteDao buildDao() {
    return buildDao(mypivotJdbcTemplateMock);
  }

  private TreasuryCsvCompleteDao buildDao(NamedParameterJdbcTemplate mypivotJdbcTemplate) {
    when(sqlLoaderMock.load("mypivot/treasury-csv-complete/treasury-csv-complete.sql")).thenReturn(FIND_BY_FILTERS_SQL);
    return new TreasuryCsvCompleteDao(mypivotJdbcTemplate, sqlLoaderMock);
  }

  private OffsetDateTime dateTime(int day) {
    return OffsetDateTime.of(LocalDateTime.of(2026, Month.JANUARY, day, 10, 30), ZoneOffset.ofHours(1));
  }

  private TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters filters(
    String logicalKey,
    OffsetDateTime createdFrom,
    OffsetDateTime createdTo,
    OffsetDateTime updatedFrom,
    OffsetDateTime updatedTo
  ) {
    return new TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters(
      logicalKey,
      createdFrom,
      createdTo,
      updatedFrom,
      updatedTo
    );
  }
}
