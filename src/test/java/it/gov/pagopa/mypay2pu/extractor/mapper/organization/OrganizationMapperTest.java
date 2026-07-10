package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.faker.OrganizationFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

  @Mock
  private OrganizationDao organizationDaoMock;
  private final OrganizationFaker organizationFaker = new OrganizationFaker();

  @AfterEach
  void assertNoMoreInteractions() {
    verifyNoMoreInteractions(organizationDaoMock);
  }

  @Test
  void mapShouldPopulateExportDto() {
    ExtractorExportProperties exportProperties = new ExtractorExportProperties("12345678901");
    OrganizationMapper organizationMapper = new OrganizationMapper(organizationDaoMock, exportProperties);
    Organization organization = organizationFaker.buildOrganization();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(true);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals(organization.ipaCode(), result.getIpaCode());
    assertEquals("12345678901", result.getBrokerCf());
    assertEquals("true", result.getFlagTreasury());
    TestUtils.reflectionEqualsByName(result, organization, "externalOrganizationId", "brokerCf", "sendApiKey", "generateNoticeApiKey", "flagTreasury");
    TestUtils.checkNotNullFields(result, "externalOrganizationId", "sendApiKey", "generateNoticeApiKey");

    verify(organizationDaoMock).isTreasuryEnabled(organization.ipaCode());
  }

  @Test
  void mapShouldPreserveNullsAndTreasuryFlag() {
    ExtractorExportProperties exportProperties = new ExtractorExportProperties(null);
    OrganizationMapper organizationMapper = new OrganizationMapper(organizationDaoMock, exportProperties);
    Organization organization = organizationFaker.buildOrganizationWithNullOptionalFields();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(false);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals("false", result.getFlagTreasury());
    TestUtils.reflectionEqualsByName(result, organization, "externalOrganizationId", "brokerCf", "sendApiKey", "generateNoticeApiKey", "flagTreasury");
    TestUtils.checkNotNullFields(
      result,
      "externalOrganizationId",
      "brokerCf",
      "iban",
      "postalIban",
      "segregationCode",
      "cbillInterBankCode",
      "orgLogo",
      "additionalLanguage",
      "startDate",
      "flagNotifyIo",
      "flagNotifyIoBkp",
      "flagNotifyOutcomePush",
      "ioApiKey",
      "sendApiKey",
      "generateNoticeApiKey"
    );

    verify(organizationDaoMock).isTreasuryEnabled(organization.ipaCode());
  }
}
