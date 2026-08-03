package it.gov.pagopa.mypay2pu.extractor.mapper.orgsil;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrgSilServiceDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrgSilServiceMapperTest {

  private final OrgSilServiceMapper orgSilServiceMapper = new OrgSilServiceMapper();

  @Test
  void mapShouldPopulateExportDto() {
    OrgSilService orgSilService = TestUtils.getPodamFactory().manufacturePojo(OrgSilService.class);

    PuOrgSilServiceDTO result = orgSilServiceMapper.map(orgSilService);

    assertEquals(orgSilService.ipaCode(), result.getIpaCode());
    assertEquals(orgSilService.applicationName(), result.getApplicationName());
    assertEquals(orgSilService.serviceType(), result.getServiceType());
    assertEquals(orgSilService.serviceUrl(), result.getServiceUrl());
    assertEquals(Boolean.toString(orgSilService.flagLegacy()), result.getFlagLegacy());
    assertEquals(orgSilService.legacyJwtKid(), result.getLegacyJwtKid());
    assertEquals(orgSilService.legacyJwtSubject(), result.getLegacyJwtSubject());
    assertEquals(orgSilService.legacyJwtIssuer(), result.getLegacyJwtIssuer());
    assertEquals(orgSilService.legacyJwtAlgorithm(), result.getLegacyJwtAlgorithm());
    assertEquals(orgSilService.legacyJwtSigningKey(), result.getLegacyJwtSigningKey());
    assertEquals(orgSilService.legacyBasicAuthUrl(), result.getLegacyBasicAuthUrl());
    assertEquals(orgSilService.legacyBasicUser(), result.getLegacyBasicUser());
    assertEquals(orgSilService.legacyBasicPsw(), result.getLegacyBasicPsw());

    TestUtils.reflectionEqualsByName(result, orgSilService);
    TestUtils.checkNotNullFields(result);
  }

  @Test
  void mapShouldPreserveNullOptionalFieldsAndFalseLegacyFlag() {
    OrgSilService orgSilService = new OrgSilService(
      "IPA2",
      "app",
      "serviceType",
      "https://service.example.org",
      false,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );

    PuOrgSilServiceDTO result = orgSilServiceMapper.map(orgSilService);

    assertEquals("IPA2", result.getIpaCode());
    assertEquals("app", result.getApplicationName());
    assertEquals("serviceType", result.getServiceType());
    assertEquals("https://service.example.org", result.getServiceUrl());
    assertEquals("false", result.getFlagLegacy());
    assertNull(result.getLegacyJwtKid());
    assertNull(result.getLegacyJwtSubject());
    assertNull(result.getLegacyJwtIssuer());
    assertNull(result.getLegacyJwtAlgorithm());
    assertNull(result.getLegacyJwtSigningKey());
    assertNull(result.getLegacyBasicAuthUrl());
    assertNull(result.getLegacyBasicUser());
    assertNull(result.getLegacyBasicPsw());

    TestUtils.checkNotNullFields(
      result,
      "legacyJwtKid",
      "legacyJwtSubject",
      "legacyJwtIssuer",
      "legacyJwtAlgorithm",
      "legacyJwtSigningKey",
      "legacyBasicAuthUrl",
      "legacyBasicUser",
      "legacyBasicPsw"
    );
  }
}
