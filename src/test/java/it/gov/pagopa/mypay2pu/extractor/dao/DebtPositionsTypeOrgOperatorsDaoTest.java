package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
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
class DebtPositionsTypeOrgOperatorsDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT debt position type org operators";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenConfiguredValuesWhenFindByFiltersThenQueryMp4Database() {
    DebtPositionsTypeOrgOperatorsDao dao = buildDao();
    List<DebtPositionsTypeOrgOperators> expected = List.of(
      new DebtPositionsTypeOrgOperators("IPA_CODE", "OPERATOR_CF", "TYPE_ORG_CODE")
    );
    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA_CODE".equals(params.getValue("ipaCode"))
          && Boolean.FALSE.equals(params.getValue("operatorFiscalCodesEmpty"))
          && List.of("OPERATOR_CF").equals(params.getValue("operatorFiscalCodes"))
          && Boolean.FALSE.equals(params.getValue("debtPositionTypeOrgCodesEmpty"))
          && List.of("TYPE_ORG_CODE").equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionsTypeOrgOperatorsDao.ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPositionsTypeOrgOperators> result = dao.findByFilters(
      "IPA_CODE",
      List.of("OPERATOR_CF"),
      List.of("TYPE_ORG_CODE"),
      50,
      100
    );

    assertEquals(expected, result);
  }

  @Test
  void givenInvalidLimitWhenFindByFiltersThenThrowIllegalArgumentException() {
    DebtPositionsTypeOrgOperatorsDao dao = buildDao();
    List<String> operatorCf = List.of("OPERATOR_CF");
    List<String> typeOrgCode = List.of("TYPE_ORG_CODE");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> {
        dao.findByFilters("IPA_CODE", operatorCf, typeOrgCode, 0, 100);
      }
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  private DebtPositionsTypeOrgOperatorsDao buildDao() {
    when(sqlLoaderMock.load("mypay/debt-positions-type-org-operators/debt-positions-type-org-operators.sql"))
      .thenReturn(FIND_BY_FILTERS_SQL);
    return new DebtPositionsTypeOrgOperatorsDao(mp4JdbcTemplateMock, sqlLoaderMock);
  }
}
