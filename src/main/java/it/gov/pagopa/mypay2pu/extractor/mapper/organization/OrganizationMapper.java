package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.OrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.export.OrganizationExportDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrganizationMapper {

  public OrganizationExportDTO map (OrganizationDTO organization, OrganizationDao organizationDAO, String brokerCf) {
    return OrganizationExportDTO.builder()
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
      .brokerCf(defaultString(brokerCf))
      .ioApiKey(defaultString(organization.ioApiKey()))
      .flagTreasury(Boolean.toString(organizationDAO.isTreasuryEnabled(organization.ipaCode())))
      .sendApiKey(null)
      .generateNoticeApiKey(null)
      .build();
  }

  private String defaultString(String value) {
    return Objects.toString(value, null);
  }
}
