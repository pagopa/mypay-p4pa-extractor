package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationAdditionalLanguage;

import java.util.Map;

/**
 * Translates the MyPay4 {@code lingua_aggiuntiva} raw DB value into the
 * {@link OrganizationAdditionalLanguage} CSV value expected by the destination migration system.
 * <p>
 * The DB column already stores a 2-letter ISO language code, matching the enum's own value 1:1.
 */
public final class OrganizationAdditionalLanguageCsvConverter extends AbstractEnumCsvConverter<OrganizationAdditionalLanguage> {

  public static final OrganizationAdditionalLanguageCsvConverter INSTANCE = new OrganizationAdditionalLanguageCsvConverter();

  private OrganizationAdditionalLanguageCsvConverter() {
    super(Map.of(), OrganizationAdditionalLanguage::fromValue, OrganizationAdditionalLanguage.class, "additionalLanguage");
  }
}