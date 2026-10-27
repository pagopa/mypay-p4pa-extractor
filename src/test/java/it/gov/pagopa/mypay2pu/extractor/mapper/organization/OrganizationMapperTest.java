package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.faker.OrganizationFaker;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationAdditionalLanguage;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

  @Mock
  private OrganizationDao organizationDaoMock;
  private OrganizationMapper organizationMapper;

  @BeforeEach
  void setUp() {
    organizationMapper = new OrganizationMapper(
      organizationDaoMock,
      buildExportProperties("12345678901", "IPA_CODE")
    );
  }

  @AfterEach
  void assertNoMoreInteractions() {
    verifyNoMoreInteractions(organizationDaoMock);
  }

  @Test
  void mapShouldPopulateExportDto() {
    Organization organization = OrganizationFaker.buildOrganization();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(true);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals("12345678901", result.getBrokerCf());
    assertEquals("IPA_CODE", result.getBrokerIpaCode());
    assertEquals("true", result.getFlagTreasury());
    assertEquals(OrganizationStatus.ACTIVE, result.getStatus());
    assertEquals(OrganizationAdditionalLanguage.EN, result.getAdditionalLanguage());
    TestUtils.reflectionEqualsByName(result, organization, "status", "additionalLanguage");
    TestUtils.checkNotNullFields(result, "externalOrganizationId", "sendApiKey", "generateNoticeApiKey");

  }

  @Test
  void mapShouldPreserveNullsAndTreasuryFlag() {
    ExtractorExportProperties exportProperties = buildExportProperties("12345678901", null);
    organizationMapper = new OrganizationMapper(organizationDaoMock, exportProperties);
    Organization organization = OrganizationFaker.buildOrganizationWithNullOptionalFields();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(false);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals("12345678901", result.getBrokerCf());
    assertNull(result.getBrokerIpaCode());
    assertEquals("false", result.getFlagTreasury());
    assertEquals(OrganizationStatus.ACTIVE, result.getStatus());
    assertNull(result.getAdditionalLanguage());
    TestUtils.reflectionEqualsByName(result, organization, "externalOrganizationId", "brokerCf", "brokerIpaCode", "sendApiKey", "generateNoticeApiKey", "flagTreasury", "status", "additionalLanguage");
    TestUtils.checkNotNullFields(
      result,
      "externalOrganizationId",
      "brokerIpaCode",
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

  }

  private ExtractorExportProperties buildExportProperties(String brokerCf, String brokerIpaCode) {
    return new ExtractorExportProperties(
      "./build/extractions",
      "./build/tmp",
      brokerCf,
      brokerIpaCode,
      Map.of(MigrationFileType.ORGANIZATIONS, new ExtractorExportProperties.FileTypeConfiguration(500))
    );
  }
}
