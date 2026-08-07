package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
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
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionDaoTest {

  private static final String FIND_DEBT_POSITIONS_SQL = "SELECT open debt positions";
  private static final String FIND_CANCELLED_DEBT_POSITIONS_SQL = "SELECT cancelled debt positions";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenOpenFiltersWhenFindDebtPositionsThenQueryMp4Database() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());
    ExtractionFilters filters = new ExtractionFilters()
      .logicalKey("IUV-1")
      .dateFrom(LocalDate.of(2026, Month.JANUARY, 10))
      .dateTo(LocalDate.of(2026, Month.JANUARY, 12));

    when(mp4JdbcTemplateMock.query(
      eq(FIND_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.FALSE.equals(params.getValue("skipCodIuvFilter"))
          && List.of("IUV-1").equals(params.getValue("iuvs"))
          && Boolean.FALSE.equals(params.getValue("skipDateFromFilter"))
          && LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay().equals(params.getValue("dateFrom"))
          && Boolean.FALSE.equals(params.getValue("skipDateToExclusiveFilter"))
          && LocalDate.of(2026, Month.JANUARY, 13).atStartOfDay().equals(params.getValue("dateToExclusive"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findDebtPositions(
      "IPA1",
      filters,
      50,
      100
    );

    assertEquals(expected, result);
  }

  @Test
  void givenNullOptionalFiltersWhenFindCancelledDebtPositionsThenQueryMp4Database() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());
    ExtractionFilters filters = new ExtractionFilters().logicalKey("IUV-2");

    when(mp4JdbcTemplateMock.query(
      eq(FIND_CANCELLED_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA2".equals(params.getValue("codIpaEnte"))
          && Boolean.FALSE.equals(params.getValue("skipCodIuvFilter"))
          && List.of("IUV-2").equals(params.getValue("iuvs"))
          && Boolean.TRUE.equals(params.getValue("skipDateFromFilter"))
          && params.hasValue("dateFrom")
          && params.getValue("dateFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipDateToExclusiveFilter"))
          && params.hasValue("dateToExclusive")
          && params.getValue("dateToExclusive") == null
          && Integer.valueOf(Integer.MAX_VALUE).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findCancelledDebtPositions("IPA2", filters, Integer.MAX_VALUE, 0);

    assertEquals(expected, result);
  }

  @Test
  void givenNullLogicalKeyWhenFindDebtPositionsThenQueryMp4DatabaseWithoutIuvFilter() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());
    ExtractionFilters filters = new ExtractionFilters();

    when(mp4JdbcTemplateMock.query(
      eq(FIND_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.TRUE.equals(params.getValue("skipCodIuvFilter"))
          && List.of("").equals(params.getValue("iuvs"))
          && Boolean.TRUE.equals(params.getValue("skipDateFromFilter"))
          && params.getValue("dateFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipDateToExclusiveFilter"))
          && params.getValue("dateToExclusive") == null
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findDebtPositions("IPA1", filters, 50, 0);

    assertEquals(expected, result);
  }

  @Test
  void givenInvalidLimitWhenFindDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();
    ExtractionFilters filters = new ExtractionFilters().logicalKey("IUV-1");

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        dao.findDebtPositions("IPA1", filters, 0, 0);
      },
      "limit must be greater than 0"
    );

  }

  @Test
  void givenInvalidLogicalKeyWhenFindDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();
    ExtractionFilters filters = new ExtractionFilters().logicalKey("IUV-1|IUD-1");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findDebtPositions("IPA1", filters, 10, 0)
    );

    assertEquals("filters.logicalKey must be a non-empty comma-separated list", exception.getMessage());
  }

  @Test
  void givenEmptyCodIpaEnteWhenFindCancelledDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findCancelledDebtPositions("", null, 10, 0)
    );

    assertEquals("codIpaEnte must not be blank", exception.getMessage());
  }

  private DebtPositionDao buildDao() {
    when(sqlLoaderMock.load("mypay/debt-positions/debt-positions-open.sql")).thenReturn(FIND_DEBT_POSITIONS_SQL);
    when(sqlLoaderMock.load("mypay/debt-positions/debt-positions-cancelled.sql")).thenReturn(FIND_CANCELLED_DEBT_POSITIONS_SQL);
    return new DebtPositionDao(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  private DebtPosition buildDebtPosition() {
    return new DebtPosition(
      "IUPD-1",
      "description",
      LocalDate.of(2026, Month.JANUARY, 15),
      false,
      LocalDate.of(2026, Month.JANUARY, 16),
      1,
      "SINGLE_INSTALLMENT",
      "Pagamento Singolo Avviso",
      "IUD-1",
      "IUV-1",
      "F",
      "CF123",
      "John Doe",
      "Street",
      "10",
      "00100",
      "Rome",
      "RM",
      "IT",
      "john.doe@example.com",
      LocalDate.of(2026, Month.JANUARY, 20),
      BigDecimal.TEN,
      "TAX",
      "remittance",
      "metadata",
      true,
      "balance",
      false,
      true,
      "CFENTE",
      "Ente",
      "IT60X0542811101000000123456",
      "causale",
      BigDecimal.ONE,
      "9/0101101IM/",
      LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(),
      LocalDate.of(2026, Month.JANUARY, 11).atStartOfDay()
    );
  }
}
