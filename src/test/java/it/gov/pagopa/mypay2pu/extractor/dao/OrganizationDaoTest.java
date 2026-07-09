package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.OrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.utils.SqlLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationDaoTest {

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplate;
  @Mock
  private NamedParameterJdbcTemplate mpv4JdbcTemplate;
  @Mock
  private SqlLoader sqlLoader;

  @Test
  void givenFiltersWhenFindByFiltersThenQueryMp4Database() {
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, mpv4JdbcTemplate, sqlLoader);

    List<OrganizationDTO> expected = List.of(new OrganizationDTO(
      "IPA1", "1", "CF", "name", "type", "mail@example.com",
      "iban", "postal", "seg", "cbill", "logo", "ACTIVE",
      "it", null, true, false
    ));
    when(mp4JdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
      .thenReturn(expected);

    List<OrganizationDTO> result = dao.findByFilters("IPA1", new ExtractionFilters(null, null));

    assertEquals(expected, result);
    verify(mp4JdbcTemplate).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
  }

  @Test
  void givenPagedFiltersWhenFindByFiltersThenUsePagedSqlAndParams() {
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations ORDER BY id LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, mpv4JdbcTemplate, sqlLoader);

    when(mp4JdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
      .thenReturn(List.of());

    dao.findByFilters("IPA1", new ExtractionFilters(null, null), 50, 100);

    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
    verify(mp4JdbcTemplate).query(queryCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));
    assertTrue(queryCaptor.getValue().contains("LIMIT :limit OFFSET :offset"));
    assertEquals(50, paramsCaptor.getValue().getValue("limit"));
    assertEquals(100, paramsCaptor.getValue().getValue("offset"));
    assertEquals("IPA1", paramsCaptor.getValue().getValue("ipaCode"));
  }

  @Test
  void givenInvalidLimitWhenFindByFiltersThenThrowIllegalArgumentException() {
    ExtractionFilters filters = new ExtractionFilters(null, null);
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, mpv4JdbcTemplate, sqlLoader);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> {
        dao.findByFilters("IPA1", filters, 0, 0);
      }
    );
    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenMissingPivotTemplateWhenIsTreasuryEnabledThenReturnFalse() {
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, null, sqlLoader);

    assertFalse(dao.isTreasuryEnabled("IPA1"));
  }

  @Test
  void givenNullIpaCodeWhenIsTreasuryEnabledThenReturnFalse() {
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, mpv4JdbcTemplate, sqlLoader);

    assertFalse(dao.isTreasuryEnabled(null));
    verify(mpv4JdbcTemplate, never()).queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class));
  }

  @Test
  void givenPivotResultWhenIsTreasuryEnabledThenReturnBooleanValue() {
    when(sqlLoader.load("mypay/organization/organization.sql")).thenReturn("SELECT * FROM organizations LIMIT :limit OFFSET :offset");
    when(sqlLoader.load("mypivot/organization/organization-pivot.sql")).thenReturn("SELECT TRUE");
    OrganizationDao dao = new OrganizationDao(mp4JdbcTemplate, mpv4JdbcTemplate, sqlLoader);

    when(mpv4JdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
      .thenReturn(Boolean.TRUE)
      .thenReturn(Boolean.FALSE);

    assertTrue(dao.isTreasuryEnabled("IPA1"));
    assertFalse(dao.isTreasuryEnabled("IPA1"));
  }
}
