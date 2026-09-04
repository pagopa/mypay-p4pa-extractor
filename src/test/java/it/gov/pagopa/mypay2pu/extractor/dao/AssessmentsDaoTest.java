package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentsDaoTest {

  private static final String FIND_BY_FILTERS_SQL = "SELECT assessments";

  @Mock
  private NamedParameterJdbcTemplate mypivotJdbcTemplateMock;

  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mypivotJdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenNoAssessmentCodesAndNoDateRangeWhenFindByFiltersThenUseIncrementalFilterParams() {
    AssessmentsDao dao = buildDao();
    OffsetDateTime lastExtractionDate = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0),
      ZoneOffset.UTC
    );

    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && lastExtractionDate.equals(params.getValue("lastExtractionDate"))
          && Boolean.TRUE.equals(params.getValue("skipAssessmentCodesFilter"))
          && Collections.singletonList(null).equals(params.getValue("assessmentCodes"))
          && params.getValue("dateFrom") == null
          && params.getValue("dateTo") == null
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
          && params.getValues().size() == 8
      ),
      same(AssessmentsDao.ASSESSMENTS_ROW_MAPPER)
    )).thenReturn(List.of());

    List<Assessments> result = dao.findByFilters("IPA1", lastExtractionDate, null, null, null, 50, 0);

    assertEquals(List.of(), result);
  }

  @Test
  void givenAssessmentCodesAndDateRangeWhenFindByFiltersThenApplyBothFilters() {
    AssessmentsDao dao = buildDao();
    List<String> assessmentCodes = List.of("ASSESSMENT-1", "ASSESSMENT-2");
    OffsetDateTime from = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 0, 0),
      ZoneOffset.UTC
    );
    OffsetDateTime to = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 12, 0, 0),
      ZoneOffset.UTC
    );
    OffsetDateTime lastExtractionDate = OffsetDateTime.of(
      LocalDateTime.of(2025, Month.DECEMBER, 31, 0, 0),
      ZoneOffset.UTC
    );

    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && lastExtractionDate.equals(params.getValue("lastExtractionDate"))
          && Boolean.FALSE.equals(params.getValue("skipAssessmentCodesFilter"))
          && assessmentCodes.equals(params.getValue("assessmentCodes"))
          && from.equals(params.getValue("dateFrom"))
          && to.equals(params.getValue("dateTo"))
          && Integer.valueOf(25).equals(params.getValue("limit"))
          && Integer.valueOf(10).equals(params.getValue("offset"))
          && params.getValues().size() == 8
      ),
      same(AssessmentsDao.ASSESSMENTS_ROW_MAPPER)
    )).thenReturn(List.of());

    List<Assessments> result = dao.findByFilters("IPA1", lastExtractionDate, assessmentCodes, from, to, 25, 10);

    assertEquals(List.of(), result);
  }

  @Test
  void givenEmptyAssessmentCodesAndDateRangeWhenFindByFiltersThenSkipAssessmentCodesFilter() {
    AssessmentsDao dao = buildDao();
    OffsetDateTime from = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 10, 0, 0),
      ZoneOffset.UTC
    );
    OffsetDateTime to = OffsetDateTime.of(
      LocalDateTime.of(2026, Month.JANUARY, 12, 0, 0),
      ZoneOffset.UTC
    );

    when(mypivotJdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        Boolean.TRUE.equals(params.getValue("skipAssessmentCodesFilter"))
          && Collections.singletonList(null).equals(params.getValue("assessmentCodes"))
          && from.equals(params.getValue("dateFrom"))
          && to.equals(params.getValue("dateTo"))
      ),
      same(AssessmentsDao.ASSESSMENTS_ROW_MAPPER)
    )).thenReturn(List.of());

    assertEquals(List.of(), dao.findByFilters("IPA1", null, List.of(), from, to, 10, 0));
  }

  @Test
  void givenInvalidLimitWhenFindByFiltersThenThrowIllegalArgumentException() {
    AssessmentsDao dao = buildDao();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters("IPA1", null, null, null, null, 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  private AssessmentsDao buildDao() {
    when(sqlLoaderMock.load("mypivot/assessments/assessments-export.sql")).thenReturn(FIND_BY_FILTERS_SQL);
    return new AssessmentsDao(mypivotJdbcTemplateMock, sqlLoaderMock);
  }
}
