package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import it.gov.pagopa.mypay2pu.extractor.utils.faker.OrganizationFaker;
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

  private static final String TRANSLATED_STATUS = "TRANSLATED_STATUS";
  private static final String TRANSLATED_ADDITIONAL_LANGUAGE = "TRANSLATED_ADDITIONAL_LANGUAGE";

  @Mock
  private OrganizationDao organizationDaoMock;
  @Mock
  private OrganizationStatusCsvConverter statusCsvConverterMock;
  @Mock
  private OrganizationAdditionalLanguageCsvConverter additionalLanguageCsvConverterMock;
  private OrganizationMapper organizationMapper;

  @BeforeEach
  void setUp() {
    organizationMapper = new OrganizationMapper(
      organizationDaoMock,
      buildExportProperties("12345678901", "IPA_CODE"),
      statusCsvConverterMock,
      additionalLanguageCsvConverterMock
    );
  }

  @AfterEach
  void assertNoMoreInteractions() {
    verifyNoMoreInteractions(organizationDaoMock, statusCsvConverterMock, additionalLanguageCsvConverterMock);
  }

  @Test
  void mapShouldPopulateExportDto() {
    Organization organization = OrganizationFaker.buildOrganization();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(true);
    when(statusCsvConverterMock.toCsvValue(organization.status())).thenReturn(TRANSLATED_STATUS);
    when(additionalLanguageCsvConverterMock.toCsvValue(organization.additionalLanguage())).thenReturn(TRANSLATED_ADDITIONAL_LANGUAGE);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals("12345678901", result.getBrokerCf());
    assertEquals("IPA_CODE", result.getBrokerIpaCode());
    assertEquals("true", result.getFlagTreasury());
    assertEquals(TRANSLATED_STATUS, result.getStatus());
    assertEquals(TRANSLATED_ADDITIONAL_LANGUAGE, result.getAdditionalLanguage());
    TestUtils.reflectionEqualsByName(result, organization, "status", "additionalLanguage");
    TestUtils.checkNotNullFields(result, "externalOrganizationId", "sendApiKey", "generateNoticeApiKey");

  }

  @Test
  void mapShouldPreserveNullsAndTreasuryFlag() {
    ExtractorExportProperties exportProperties = buildExportProperties("12345678901", null);
    organizationMapper = new OrganizationMapper(organizationDaoMock, exportProperties, statusCsvConverterMock, additionalLanguageCsvConverterMock);
    Organization organization = OrganizationFaker.buildOrganizationWithNullOptionalFields();

    when(organizationDaoMock.isTreasuryEnabled(organization.ipaCode())).thenReturn(false);
    when(statusCsvConverterMock.toCsvValue(organization.status())).thenReturn(TRANSLATED_STATUS);
    when(additionalLanguageCsvConverterMock.toCsvValue(null)).thenReturn(null);

    PuOrganizationDTO result = organizationMapper.map(organization);

    assertEquals("12345678901", result.getBrokerCf());
    assertNull(result.getBrokerIpaCode());
    assertEquals("false", result.getFlagTreasury());
    assertEquals(TRANSLATED_STATUS, result.getStatus());
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
      brokerCf,
      brokerIpaCode,
      Map.of(MigrationFileType.ORGANIZATIONS, new ExtractorExportProperties.FileTypeConfiguration(500))
    );
  }
}
