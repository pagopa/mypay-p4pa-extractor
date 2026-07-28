package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgDaoTest {

  private static final String FIND_BY_ORGANIZATION_ID_SQL = "SELECT debt position types by organization";
  private static final String IS_EXTERNAL_SQL_PATH = "SELECT TRUE";

  @Mock
  private NamedParameterJdbcTemplate mp4JdbcTemplateMock;
  @Mock
  private NamedParameterJdbcTemplate mypivotJdbcTemplateMock;
  @Mock
  private SqlLoader sqlLoaderMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(mp4JdbcTemplateMock, mypivotJdbcTemplateMock, sqlLoaderMock);
  }

  @Test
  void givenOrganizationWithoutLogicalKeysWhenFindThenQueryPagedMp4Database() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);
    List<DebtPositionTypeOrg> expected = List.of(sourceRow());

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_ORGANIZATION_ID_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && Boolean.TRUE.equals(params.getValue("skipDebtPositionTypeOrgCodesFilter"))
          && containsOnlyNullLogicalKey(params)
          && Integer.valueOf(50).equals(params.getValue("limit"))
          && Integer.valueOf(100).equals(params.getValue("offset"))
          && params.getValues().size() == 5
      ),
      same(DebtPositionTypeOrgDao.DEBT_POSITION_TYPE_ORG_ROW_MAPPER)
    )).thenReturn(expected);

    List<DebtPositionTypeOrg> result = dao.findByFilters("IPA1", null, 50, 100);

    assertEquals(expected, result);
  }

  @Test
  void givenLogicalKeysWhenFindThenPassThemUnchangedToMp4Database() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);
    List<String> logicalKeys = List.of("TAX", "FEE");

    when(mp4JdbcTemplateMock.query(
      eq(FIND_BY_ORGANIZATION_ID_SQL),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && Boolean.FALSE.equals(params.getValue("skipDebtPositionTypeOrgCodesFilter"))
          && logicalKeys.equals(params.getValue("debtPositionTypeOrgCodes"))
          && Integer.valueOf(25).equals(params.getValue("limit"))
          && Integer.valueOf(0).equals(params.getValue("offset"))
          && params.getValues().size() == 5
      ),
      same(DebtPositionTypeOrgDao.DEBT_POSITION_TYPE_ORG_ROW_MAPPER)
    )).thenReturn(List.of(sourceRowWithNullableValues()));

    List<DebtPositionTypeOrg> result = dao.findByFilters("IPA1", logicalKeys, 25, 0);

    assertEquals(List.of(sourceRowWithNullableValues()), result);
  }

  @Test
  void givenBlankOrganizationIdWhenFindThenRejectBeforeDatabaseInteraction() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);
    List<String> keys = List.of("TAX");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters(" ", keys, 10, 0)
    );

    assertEquals("ipaCode must not be blank", exception.getMessage());
  }

  @Test
  void givenInvalidPagingWhenFindThenRejectBeforeDatabaseInteraction() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);
    List<String> keys = List.of("TAX");

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> dao.findByFilters("IPA1", keys, 0, 0)
    );

    assertEquals("limit must be greater than 0", exception.getMessage());
  }

  @Test
  void givenMissingMypivotTemplateWhenCheckExternalThenReturnFalse() {
    DebtPositionTypeOrgDao dao = buildDao(null);

    assertFalse(dao.isExternal("IPA1", "TAX"));
  }

  @Test
  void givenMypivotResultWhenCheckExternalThenReturnBooleanValue() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);

    when(mypivotJdbcTemplateMock.queryForObject(
      eq(IS_EXTERNAL_SQL_PATH),
      ArgumentMatchers.<MapSqlParameterSource>argThat(params ->
        "IPA1".equals(params.getValue("ipaCode"))
          && "TAX".equals(params.getValue("debtPositionsTypeOrgCode"))
          && params.getValues().size() == 2
      ),
      eq(Boolean.class)
    )).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE).thenReturn(null);

    assertTrue(dao.isExternal("IPA1", "TAX"));
    assertFalse(dao.isExternal("IPA1", "TAX"));
    assertFalse(dao.isExternal("IPA1", "TAX"));
  }

  @Test
  void givenBlankExternalLookupKeyWhenCheckExternalThenReturnFalse() {
    DebtPositionTypeOrgDao dao = buildDao(mypivotJdbcTemplateMock);

    assertFalse(dao.isExternal(" ", "TAX"));
    assertFalse(dao.isExternal("IPA1", " "));
  }

  private DebtPositionTypeOrgDao buildDao(NamedParameterJdbcTemplate mypivotJdbcTemplate) {
    when(sqlLoaderMock.load("mypay/debt-position-type-org/debt-position-type-org.sql"))
      .thenReturn(FIND_BY_ORGANIZATION_ID_SQL);
    when(sqlLoaderMock.load("mypivot/debt-position-type-org/is-external.sql")).thenReturn(IS_EXTERNAL_SQL_PATH);
    return new DebtPositionTypeOrgDao(mp4JdbcTemplateMock, mypivotJdbcTemplate, sqlLoaderMock);
  }

  private boolean containsOnlyNullLogicalKey(MapSqlParameterSource params) {
    Object logicalKeys = params.getValue("debtPositionTypeOrgCodes");
    return logicalKeys instanceof List<?> values && values.size() == 1 && values.getFirst() == null;
  }

  private DebtPositionTypeOrg sourceRow() {
    return new DebtPositionTypeOrg(
      "IPA1", "BILANCIO", "TAX", "Tax", "IT60X0542811101000000123456",
      "IT60X0542811101000000123456", "123456", "Municipality", "Public administration",
      1234L, "https://example.test/pay", false, true, true, false, false,
       "PAYMENT_NOTIFICATION", true, "SERVICE", "XSD");
  }

  private DebtPositionTypeOrg sourceRowWithNullableValues() {
    return new DebtPositionTypeOrg(
      "IPA1", null, "FEE", "Fee", "IT60X0542811101000000123456",
      null, null, null, null, null, null, false, false,
      false, false, false, null, false, null, null
    );
  }
}
