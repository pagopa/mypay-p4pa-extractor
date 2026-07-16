package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Translates MyPay4 {@code cod_stato} values into the {@link OrganizationStatus} CSV values
 * expected by the destination migration system.
 */
@Component
public class OrganizationStatusCsvConverter extends AbstractEnumCsvConverter<OrganizationStatus> {

  @Override
  protected Function<String, OrganizationStatus> defaultResolver() {
    return OrganizationStatus::fromValue;
  }

  @Override
  protected Function<OrganizationStatus, String> csvValueExtractor() {
    return OrganizationStatus::getValue;
  }

  @Override
  protected Class<OrganizationStatus> enumType() {
    return OrganizationStatus.class;
  }

  @Override
  protected String csvFieldName() {
    return "status";
  }

  @Override
  protected Map<String, OrganizationStatus> explicitMappings() {
    return Map.of(
      "ESERCIZIO", OrganizationStatus.ACTIVE,
      "INSERITO", OrganizationStatus.DRAFT,
      "PRE-ESERCIZIO", OrganizationStatus.DRAFT
    );
  }
}