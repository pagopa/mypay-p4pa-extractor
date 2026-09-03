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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentsDaoTest {

  private static final String FIND_BY_LAST_MIGRATION_SQL = "SELECT assessments by last migration";
  private static final String FIND_BY_DATE_RANGE_SQL = "SELECT assessments by date range";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenNoDateRangeWhenFindByFiltersThenUseLastMigrationSql() {
    AssessmentsDao dao = buildDao();

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_LAST_MIGRATION_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && params.getValue("dataUltimaMigrazione") == null
          && params.getValues().size() == 2
      ),
      same(AssessmentsDao.ASSESSMENTS_ROW_MAPPER)
    )).thenReturn(List.of());

    List<Assessments> result = dao.findByFilters("IPA1", null, null);

    assertEquals(List.of(), result);
  }

  @Test
  void givenDateRangeWhenFindByFiltersThenUseDateRangeSql() {
    AssessmentsDao dao = buildDao();
    OffsetDateTime from = OffsetDateTime.of(2026, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime to = OffsetDateTime.of(2026, 1, 12, 0, 0, 0, 0, ZoneOffset.UTC);

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_DATE_RANGE_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("codIpaEnte"))
          && from.equals(params.getValue("dateFrom"))
          && to.equals(params.getValue("dateTo"))
          && params.getValues().size() == 3
      ),
      same(AssessmentsDao.ASSESSMENTS_ROW_MAPPER)
    )).thenReturn(List.of());

    List<Assessments> result = dao.findByFilters("IPA1", from, to);

    assertEquals(List.of(), result);
  }

  private AssessmentsDao buildDao() {
    when(sqlLoaderMock.load("mypay/assessments/assessments-export-by-last-migration.sql")).thenReturn(FIND_BY_LAST_MIGRATION_SQL);
    when(sqlLoaderMock.load("mypay/assessments/assessments-export-by-date-range.sql")).thenReturn(FIND_BY_DATE_RANGE_SQL);
    return new AssessmentsDao(mp4JdbcTemplateMock, sqlLoaderMock);
  }
}
