package it.gov.pagopa.mypay2pu.extractor.mapper.organization;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationAdditionalLanguage;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Translates the MyPay4 {@code lingua_aggiuntiva} raw DB value into the
 * {@link OrganizationAdditionalLanguage} CSV value expected by the destination migration system.
 * <p>
 * The DB column already stores a 2-letter ISO language code, matching the enum's own value 1:1.
 */
@Component
public class OrganizationAdditionalLanguageCsvConverter extends AbstractEnumCsvConverter<OrganizationAdditionalLanguage> {

  @Override
  protected Function<String, OrganizationAdditionalLanguage> defaultResolver() {
    return OrganizationAdditionalLanguage::fromValue;
  }

  @Override
  protected Function<OrganizationAdditionalLanguage, String> csvValueExtractor() {
    return OrganizationAdditionalLanguage::getValue;
  }

  @Override
  protected Class<OrganizationAdditionalLanguage> enumType() {
    return OrganizationAdditionalLanguage.class;
  }

  @Override
  protected String csvFieldName() {
    return "additionalLanguage";
  }
}