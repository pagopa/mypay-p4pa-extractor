package it.gov.pagopa.mypay2pu.extractor.dao;

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

    when(mp4JdbcTemplateMock.query(
      eq(FIND_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("organizationId"))
          && "IUPD-1".equals(params.getValue("gpdIupd"))
          && "IUD-1".equals(params.getValue("codIud"))
          && LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay().equals(params.getValue("updatedFrom"))
          && LocalDate.of(2026, Month.JANUARY, 13).atStartOfDay().equals(params.getValue("updatedToExclusive"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findDebtPositions(
      "IPA1",
      "IUPD-1",
      "IUD-1",
      LocalDate.of(2026, Month.JANUARY, 10),
      LocalDate.of(2026, Month.JANUARY, 12),
      50,
      100
    );

    assertEquals(expected, result);
  }

  @Test
  void givenNullOptionalFiltersWhenFindCancelledDebtPositionsThenQueryMp4Database() {
    DebtPositionDao dao = buildDao();
    List<DebtPosition> expected = List.of(buildDebtPosition());

    when(mp4JdbcTemplateMock.query(
      eq(FIND_CANCELLED_DEBT_POSITIONS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA2".equals(params.getValue("organizationId"))
          && params.hasValue("gpdIupd")
          && params.getValue("gpdIupd") == null
          && params.hasValue("codIud")
          && params.getValue("codIud") == null
          && params.hasValue("updatedFrom")
          && params.getValue("updatedFrom") == null
          && params.hasValue("updatedToExclusive")
          && params.getValue("updatedToExclusive") == null
          && Integer.valueOf(Integer.MAX_VALUE).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
      ),
      same(DebtPositionDao.DEBT_POSITION_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPosition> result = dao.findCancelledDebtPositions("IPA2", null, null, null, null);

    assertEquals(expected, result);
  }

  @Test
  void givenInvalidLimitWhenFindDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findDebtPositions("IPA1", null, null, null, null, 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenBlankOrganizationIdWhenFindCancelledDebtPositionsThenThrowIllegalArgumentException() {
    DebtPositionDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findCancelledDebtPositions(" ", null, null, null, null)
    );

    assertEquals("organizationId must not be blank", exception.getMessage());
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
      "I",
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
