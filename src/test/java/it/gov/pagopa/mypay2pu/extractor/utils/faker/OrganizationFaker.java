package it.gov.pagopa.mypay2pu.extractor.utils.faker;

import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import uk.co.jemos.podam.api.PodamFactory;

public class OrganizationFaker {

  private final PodamFactory podamFactory;

  public OrganizationFaker() {
    this(TestUtils.getPodamFactory());
  }

  public OrganizationFaker(PodamFactory podamFactory) {
    this.podamFactory = podamFactory;
  }

  public Organization buildOrganization() {
    return podamFactory.manufacturePojo(Organization.class);
  }

  public Organization buildOrganizationWithNullOptionalFields() {
    Organization source = buildOrganization();
    return new Organization(
      source.ipaCode(),
      null,
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
