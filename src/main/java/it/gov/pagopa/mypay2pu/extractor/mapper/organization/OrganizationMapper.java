package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrganizationMapper {

  private final OrganizationDao organizationDao;
  private final ExtractorExportProperties exportProperties;

  public OrganizationMapper(OrganizationDao organizationDao, ExtractorExportProperties exportProperties) {
    this.organizationDao = organizationDao;
    this.exportProperties = exportProperties;
  }

  public PuOrganizationDTO map(Organization organization) {
    return PuOrganizationDTO.builder()
      .ipaCode(defaultString(organization.ipaCode()))
      .externalOrganizationId(null)
      .orgFiscalCode(defaultString(organization.orgFiscalCode()))
      .orgName(defaultString(organization.orgName()))
      .orgTypeCode(defaultString(organization.orgTypeCode()))
      .orgEmail(defaultString(organization.orgEmail()))
      .iban(defaultString(organization.iban()))
      .postalIban(defaultString(organization.postalIban()))
      .segregationCode(defaultString(organization.segregationCode()))
      .cbillInterBankCode(defaultString(organization.cbillInterBankCode()))
      .orgLogo(defaultString(organization.orgLogo()))
      .additionalLanguage(defaultString(organization.additionalLanguage()))
      .startDate(organization.startDate())
      .flagNotifyIo(organization.flagNotifyIo())
      .flagNotifyIoBkp(organization.flagNotifyIoBkp())
      .flagNotifyOutcomePush(organization.flagNotifyOutcomePush())
      .status(defaultString(organization.status()))
      .brokerCf(defaultString(exportProperties.brokerCf()))
      .ioApiKey(defaultString(organization.ioApiKey()))
      .flagTreasury(Boolean.toString(organizationDao.isTreasuryEnabled(organization.ipaCode())))
      .sendApiKey(null)
      .generateNoticeApiKey(null)
      .build();
  }

  private String defaultString(String value) {
    return Objects.toString(value, null);
  }
}
