package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReportingMyPivotDaoTest {

  private static final String FIND_FLOW_IDENTIFIERS_SQL = "SELECT payment reporting flow identifiers";

  @Mock
  private NamedParameterJdbcTemplate mypivotJdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(mypivotJdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenFiltersWhenFindFlowIdentifiersThenQueryMyPivot() {
    OffsetDateTime createdFrom = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 30),
      ZoneOffset.UTC
    );
    OffsetDateTime createdTo = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 11, 10, 30),
      ZoneOffset.UTC
    );
    List<String> expected = List.of("FLOW_1");
    PaymentReportingMyPivotDao dao = buildDao();
    when(mypivotJdbcTemplateMock.queryForList(
      eq(FIND_FLOW_IDENTIFIERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && createdFrom.toLocalDateTime().equals(params.getValue("createdFrom"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedFromFilter"))
          && createdTo.toLocalDateTime().equals(params.getValue("createdTo"))
          && Boolean.FALSE.equals(params.getValue("skipCreatedToFilter"))
          && Boolean.FALSE.equals(params.getValue("skipFlowIdentifiersFilter"))
          && List.of("FLOW_1").equals(params.getValue("flowIdentifiers"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
          && params.getValues().size() == 9
      ),
      eq(String.class)
    )).thenReturn(expected);

    assertEquals(expected, dao.findFlowIdentifiers(
      "IPA1", null, createdFrom, createdTo, List.of("FLOW_1"), 50, 100
    ));
  }

  @Test
  void givenNoFiltersWhenFindFlowIdentifiersThenSkipOptionalFilters() {
    PaymentReportingMyPivotDao dao = buildDao();
    when(mypivotJdbcTemplateMock.queryForList(
      eq(FIND_FLOW_IDENTIFIERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        params.getValue("createdFrom") == null
          && Boolean.TRUE.equals(params.getValue("skipCreatedFromFilter"))
          && params.getValue("createdTo") == null
          && Boolean.TRUE.equals(params.getValue("skipCreatedToFilter"))
          && Boolean.TRUE.equals(params.getValue("skipFlowIdentifiersFilter"))
          && Collections.singletonList(null).equals(params.getValue("flowIdentifiers"))
      ),
      eq(String.class)
    )).thenReturn(List.of());

    assertEquals(List.of(), dao.findFlowIdentifiers("IPA1", null, null, null, List.of(), 1, 0));
  }

  @Test
  void givenSqlWhenVerifiedThenContainFlowSelectionParameters() {
    SqlTestUtils.assertQueryParameters(
      PaymentReportingMyPivotDao.FIND_FLOW_IDENTIFIERS_SQL_PATH,
      List.of(
        "ipaCode",
        "skipCreatedFromFilter",
        "createdFrom",
        "skipCreatedToFilter",
        "createdTo",
        "skipFlowIdentifiersFilter",
        "flowIdentifiers",
        "limit",
        "offset"
      )
    );
  }

  private PaymentReportingMyPivotDao buildDao() {
    when(sqlLoaderMock.load(PaymentReportingMyPivotDao.FIND_FLOW_IDENTIFIERS_SQL_PATH))
      .thenReturn(FIND_FLOW_IDENTIFIERS_SQL);
    return new PaymentReportingMyPivotDao(mypivotJdbcTemplateMock, sqlLoaderMock);
  }
}
