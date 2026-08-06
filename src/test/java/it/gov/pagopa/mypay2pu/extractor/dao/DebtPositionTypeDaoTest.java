package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeDaoTest {

  private static final String FIND_ALL_SQL = "SELECT debt position types";

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
    DebtPositionTypeDao dao = buildDao();
    List<DebtPositionType> expected = List.of(new DebtPositionType(
      "12345678901",
      "TAX",
      "Tax",
      "COMUNE",
      "AREA",
      "SERVICE",
      "REASON",
      "01.01.01",
      false,
      false,
      false
    ));
    when(mp4JdbcTemplateMock.query(
      eq(FIND_ALL_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "12345678901".equals(params.getValue("brokerCf"))
          && Boolean.FALSE.equals(params.getValue("skipDebtPositionTypeOrgCodesFilter"))
          && List.of("TYPE_ORG").equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      same(DebtPositionTypeDao.DEBT_POSITIONS_TYPE_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPositionType> result = dao.findByFilters(List.of("TYPE_ORG"), 50, 100);

    assertEquals(expected, result);
  }

  private DebtPositionTypeDao buildDao() {
    when(sqlLoaderMock.load("mypay/debt-position-type/debt-position-type.sql")).thenReturn(FIND_ALL_SQL);
    return new DebtPositionTypeDao(
      mp4JdbcTemplateMock,
      new ExtractorExportProperties("./build", "./build", "12345678901", "IPA_CODE", Map.of()),
      sqlLoaderMock
    );
  }
}
