package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDaoTest {
  private static final String FIND_BY_FILTERS_SQL = "SELECT * FROM organizations ORDER BY id LIMIT :limit OFFSET :offset";
  private static final String FIND_TREASURY_BY_IPA_SQL = "SELECT TRUE";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private NamedParameterJdbcTemplate mpv4JdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, mpv4JdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenFiltersWhenFindByFiltersThenQueryMp4Database() {
    OrganizationDao dao = buildDao(mpv4JdbcTemplateMock);

    List<Organization> expected = List.of(new Organization(
      "IPA1",  "CF", "name", "type", "mail@example.com",
      "iban", "postal", "seg", "cbill", "logo", "ACTIVE",
      "it", null, true, true, false, null
    ));
    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        List.of("IPA1").equals(params.getValue("ipaCodes"))
          && !params.hasValue("limit")
          && !params.hasValue("offset")
          && params.hasValue("modifiedFrom")
          && params.getValue("modifiedFrom") == null
          && params.hasValue("modifiedToExclusive")
          && params.getValue("modifiedToExclusive") == null
      ),
      Mockito.same(OrganizationDao.ORGANIZATION_ROW_MAPPER)
    ))
      .thenReturn(expected);

    List<Organization> result = dao.findByFilters(List.of("IPA1"), new ExtractionFilters(null, null, null));

    assertEquals(expected, result);
  }

  @Test
  void givenPagedFiltersWhenFindByFiltersThenUsePagedSqlAndParams() {
    OrganizationDao dao = buildDao(mpv4JdbcTemplateMock);

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_FILTERS_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        List.of("IPA1", "IPA2").equals(params.getValue("ipaCodes"))
          && LocalDate.of(2026, Month.JANUARY, 1).atStartOfDay().equals(params.getValue("modifiedFrom"))
          && LocalDate.of(2026, Month.JANUARY, 3).atStartOfDay().equals(params.getValue("modifiedToExclusive"))
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
      ),
      Mockito.same(OrganizationDao.ORGANIZATION_ROW_MAPPER)
    ))
      .thenReturn(List.of());

    List<Organization> result = dao.findByFilters(
      List.of("IPA1", "IPA2"),
      new ExtractionFilters(LocalDate.of(2026, Month.JANUARY, 1), LocalDate.of(2026, Month.JANUARY, 2), null),
      50,
      100
    );
    assertEquals(List.of(), result);
  }

  @Test
  void givenInvalidLimitWhenFindByFiltersThenThrowIllegalArgumentException() {
    ExtractionFilters filters = new ExtractionFilters(null, null, null);
    OrganizationDao dao = buildDao(mpv4JdbcTemplateMock);
    List<String> ipaCodes = List.of("IPA1");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters(ipaCodes, filters, 0, 0)
    );
    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenMissingPivotTemplateWhenIsTreasuryEnabledThenReturnFalse() {
    OrganizationDao dao = buildDao(null);

    assertFalse(dao.isTreasuryEnabled("IPA1"));
  }

  @Test
  void givenNullIpaCodeWhenIsTreasuryEnabledThenReturnFalse() {
    OrganizationDao dao = buildDao(mpv4JdbcTemplateMock);

    assertFalse(dao.isTreasuryEnabled(null));
  }

  @Test
  void givenPivotResultWhenIsTreasuryEnabledThenReturnBooleanValue() {
    OrganizationDao dao = buildDao(mpv4JdbcTemplateMock);

    when(mpv4JdbcTemplateMock.queryForObject(
      eq(FIND_TREASURY_BY_IPA_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params -> "IPA1".equals(params.getValue("codIpaEnte"))),
      eq(Boolean.class)
    ))
      .thenReturn(Boolean.TRUE)
      .thenReturn(Boolean.FALSE);

    assertTrue(dao.isTreasuryEnabled("IPA1"));
    assertFalse(dao.isTreasuryEnabled("IPA1"));
  }

  @Test
  void givenOrganizationSqlWhenLoadedThenUsesInClauseForIpaCodes() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/mypay/organization/organization.sql"));
    assertTrue(sql.contains("ef.cod_ipa_ente IN (:ipaCodes)"));
    assertTrue(sql.contains("e.cod_ipa_ente IN (:ipaCodes)"));
  }

  private OrganizationDao buildDao(NamedParameterJdbcTemplate mpv4JdbcTemplate) {
    when(sqlLoaderMock.load("mypay/organization/organization.sql")).thenReturn(FIND_BY_FILTERS_SQL);
    when(sqlLoaderMock.load("mypivot/organization/has-treasury.sql")).thenReturn(FIND_TREASURY_BY_IPA_SQL);
    return new OrganizationDao(mp4JdbcTemplateMock, mpv4JdbcTemplate, sqlLoaderMock);
  }
}
