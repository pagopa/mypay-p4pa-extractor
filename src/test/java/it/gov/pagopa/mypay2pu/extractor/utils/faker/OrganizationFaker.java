package it.gov.pagopa.mypay2pu.extractor.utils.faker;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;

public class OrganizationFaker {

  private OrganizationFaker() {
  }

  public static Organization buildOrganization() {
    return TestUtils.getPodamFactory().manufacturePojo(Organization.class);
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
