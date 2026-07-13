package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import org.springframework.stereotype.Component;

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
      .ipaCode(organization.ipaCode())
      .externalOrganizationId(null)
      .orgFiscalCode(organization.orgFiscalCode())
      .orgName(organization.orgName())
      .orgTypeCode(organization.orgTypeCode())
      .orgEmail(organization.orgEmail())
      .iban(organization.iban())
      .postalIban(organization.postalIban())
      .segregationCode(organization.segregationCode())
      .cbillInterBankCode(organization.cbillInterBankCode())
      .orgLogo(organization.orgLogo())
      .additionalLanguage(organization.additionalLanguage())
      .startDate(organization.startDate())
      .flagNotifyIo(organization.flagNotifyIo())
      .flagNotifyIoBkp(organization.flagNotifyIoBkp())
      .flagNotifyOutcomePush(organization.flagNotifyOutcomePush())
      .status(organization.status())
      .brokerCf(exportProperties.brokerCf())
      .ioApiKey(organization.ioApiKey())
      .flagTreasury(Boolean.toString(organizationDao.isTreasuryEnabled(organization.ipaCode())))
      .sendApiKey(null)
      .generateNoticeApiKey(null)
      .build();
  }
}
