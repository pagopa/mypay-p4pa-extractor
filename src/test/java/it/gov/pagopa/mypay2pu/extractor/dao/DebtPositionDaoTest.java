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
import java.util.Collections;
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
      .modifiedFrom(LocalDate.of(2026, Month.JANUARY, 10))
      .modifiedTo(LocalDate.of(2026, Month.JANUARY, 12))
      .debtPositionTypeOrgCodes(List.of("TYPE_1", "TYPE_2"));

    when(mp4JdbcTemplateMock.query(
      eq(FIND_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.FALSE.equals(params.getValue("skipGpdIupdFilter"))
          && "IUPD-1".equals(params.getValue("gpdIupd"))
          && Boolean.FALSE.equals(params.getValue("skipCodIudFilter"))
          && "IUD-1".equals(params.getValue("codIud"))
          && Boolean.FALSE.equals(params.getValue("skipUpdatedFromFilter"))
          && LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay().equals(params.getValue("updatedFrom"))
          && Boolean.FALSE.equals(params.getValue("skipUpdatedToExclusiveFilter"))
          && LocalDate.of(2026, Month.JANUARY, 13).atStartOfDay().equals(params.getValue("updatedToExclusive"))
          && Boolean.FALSE.equals(params.getValue("debtPositionTypeOrgCodesEmpty"))
          && List.of("TYPE_1", "TYPE_2").equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findDebtPositions("IPA1", "IUPD-1", "IUD-1", filters, 50, 100);

    assertEquals(expected, result);
  }

  @Test
  void givenNullOptionalFiltersWhenFindCancelledDebtPositionsThenQueryMp4Database() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());

    when(mp4JdbcTemplateMock.query(
      eq(FIND_CANCELLED_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA2".equals(params.getValue("codIpaEnte"))
          && Boolean.TRUE.equals(params.getValue("skipGpdIupdFilter"))
          && params.hasValue("gpdIupd")
          && params.getValue("gpdIupd") == null
          && Boolean.TRUE.equals(params.getValue("skipCodIudFilter"))
          && params.hasValue("codIud")
          && params.getValue("codIud") == null
          && Boolean.TRUE.equals(params.getValue("skipUpdatedFromFilter"))
          && params.hasValue("updatedFrom")
          && params.getValue("updatedFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipUpdatedToExclusiveFilter"))
          && params.hasValue("updatedToExclusive")
          && params.getValue("updatedToExclusive") == null
          && Boolean.TRUE.equals(params.getValue("debtPositionTypeOrgCodesEmpty"))
          && Collections.singletonList(null).equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(Integer.MAX_VALUE).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findCancelledDebtPositions("IPA2", null, null, null, Integer.MAX_VALUE, 0);

    assertEquals(expected, result);
  }

  @Test
  void givenFiltersWithoutDatesWhenFindDebtPositionsThenSkipDateFilters() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());

    when(mp4JdbcTemplateMock.query(
      eq(FIND_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && Boolean.TRUE.equals(params.getValue("skipUpdatedFromFilter"))
          && params.hasValue("updatedFrom")
          && params.getValue("updatedFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipUpdatedToExclusiveFilter"))
          && params.hasValue("updatedToExclusive")
          && params.getValue("updatedToExclusive") == null
          && Boolean.TRUE.equals(params.getValue("debtPositionTypeOrgCodesEmpty"))
          && Collections.singletonList(null).equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(10).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findDebtPositions(
      "IPA1",
      null,
      null,
      new ExtractionFilters(),
      10,
      0
    );

    assertEquals(expected, result);
  }

  @Test
  void givenInvalidLimitWhenFindDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findDebtPositions("IPA1", null, null, null, 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenEmptyCodIpaEnteWhenFindCancelledDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findCancelledDebtPositions("", null, null, null, 10, 0)
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
