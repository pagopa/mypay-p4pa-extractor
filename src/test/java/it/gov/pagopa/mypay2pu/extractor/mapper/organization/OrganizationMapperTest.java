package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.OrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.export.OrganizationExportDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

  @Mock
  private OrganizationDao organizationDAO;

  @Test
  void mapShouldPopulateExportDto() {
    OrganizationMapper mapper = new OrganizationMapper();
    OrganizationDTO organization = new OrganizationDTO(
      "IPA123",
      "99",
      "12345678901",
      "Ente Demo",
      "COM",
      "ente@example.org",
      "IT60X0542811101000000123456",
      "IT60X0542811101000000654321",
      "S1",
      "12345",
      "TE9HTw==",
      "ESERCIZIO",
      "en",
      LocalDate.of(2026, 6, 18),
      true,
      false,
      true,
      "io-key"
    );

    when(organizationDAO.isTreasuryEnabled("IPA123")).thenReturn(true);

    OrganizationExportDTO result = mapper.map(organization, organizationDAO, "12345678901");

    assertEquals("IPA123", result.getIpaCode());
    assertNull(result.getExternalOrganizationId());
    assertEquals("12345678901", result.getOrgFiscalCode());
    assertEquals("Ente Demo", result.getOrgName());
    assertEquals("COM", result.getOrgTypeCode());
    assertEquals("ente@example.org", result.getOrgEmail());
    assertEquals("IT60X0542811101000000123456", result.getIban());
    assertEquals("IT60X0542811101000000654321", result.getPostalIban());
    assertEquals("S1", result.getSegregationCode());
    assertEquals("12345", result.getCbillInterBankCode());
    assertEquals("TE9HTw==", result.getOrgLogo());
    assertEquals("en", result.getAdditionalLanguage());
    assertEquals(LocalDate.of(2026, 6, 18), result.getStartDate());
    assertEquals(true, result.getFlagNotifyIo());
    assertEquals(false, result.getFlagNotifyIoBkp());
    assertEquals(true, result.getFlagNotifyOutcomePush());
    assertEquals("ESERCIZIO", result.getStatus());
    assertEquals("12345678901", result.getBrokerCf());
    assertEquals("io-key", result.getIoApiKey());
    assertEquals("true", result.getFlagTreasury());
    assertNull(result.getSendApiKey());
    assertNull(result.getGenerateNoticeApiKey());

    verify(organizationDAO).isTreasuryEnabled("IPA123");
  }

  @Test
  void mapShouldPreserveNullsAndTreasuryFlag() {
    OrganizationMapper mapper = new OrganizationMapper();
    OrganizationDTO organization = new OrganizationDTO(
      "IPA123",
      null,
      "12345678901",
      "Ente Demo",
      "COM",
      "ente@example.org",
      null,
      null,
      null,
      null,
      null,
      "ESERCIZIO",
      null,
      null,
      null,
      null,
      null,
      null
    );

    when(organizationDAO.isTreasuryEnabled("IPA123")).thenReturn(false);

    OrganizationExportDTO result = mapper.map(organization, organizationDAO, null);

    assertNull(result.getExternalOrganizationId());
    assertNull(result.getIban());
    assertNull(result.getPostalIban());
    assertNull(result.getSegregationCode());
    assertNull(result.getCbillInterBankCode());
    assertNull(result.getOrgLogo());
    assertNull(result.getAdditionalLanguage());
    assertNull(result.getStartDate());
    assertNull(result.getFlagNotifyIo());
    assertNull(result.getFlagNotifyIoBkp());
    assertNull(result.getFlagNotifyOutcomePush());
    assertNull(result.getBrokerCf());
    assertNull(result.getIoApiKey());
    assertEquals("false", result.getFlagTreasury());

    verify(organizationDAO).isTreasuryEnabled("IPA123");
  }
}
