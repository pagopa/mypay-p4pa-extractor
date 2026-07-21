package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;

import java.util.Map;

/**
 * Translates MyPay4 {@code cod_stato} values into the {@link OrganizationStatus} CSV values
 * expected by the destination migration system.
 */
public final class OrganizationStatusCsvConverter extends AbstractEnumCsvConverter<OrganizationStatus> {

  public static final OrganizationStatusCsvConverter INSTANCE = new OrganizationStatusCsvConverter();

  private OrganizationStatusCsvConverter() {
    super(Map.of(
      "ESERCIZIO", OrganizationStatus.ACTIVE,
      "INSERITO", OrganizationStatus.DRAFT,
      "PRE-ESERCIZIO", OrganizationStatus.DRAFT
    ), OrganizationStatus.class, "status");
  }
}