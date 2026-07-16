package it.gov.pagopa.mypay2pu.extractor.utils.faker;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationAdditionalLanguage;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;

public class OrganizationFaker {

  private OrganizationFaker() {
  }

  public static Organization buildOrganization() {
    Organization source = TestUtils.getPodamFactory().manufacturePojo(Organization.class);
    return new Organization(
      source.ipaCode(),
      source.orgFiscalCode(),
      source.orgName(),
      source.orgTypeCode(),
      source.orgEmail(),
      source.iban(),
      source.postalIban(),
      source.segregationCode(),
      source.cbillInterBankCode(),
      source.orgLogo(),
      OrganizationStatus.ACTIVE.getValue(),
      OrganizationAdditionalLanguage.EN.getValue(),
      source.startDate(),
      source.flagNotifyIo(),
      source.flagNotifyIoBkp(),
      source.flagNotifyOutcomePush(),
      source.ioApiKey()
    );
  }

  public static Organization buildOrganizationWithNullOptionalFields() {
    Organization source = buildOrganization();
    return new Organization(
      source.ipaCode(),
      source.orgFiscalCode(),
      source.orgName(),
      source.orgTypeCode(),
      source.orgEmail(),
      null,
      null,
      null,
      null,
      null,
      source.status(),
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
}
