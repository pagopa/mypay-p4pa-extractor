package it.gov.pagopa.mypay2pu.extractor.mapper.debtposition;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;

import java.util.Map;

public final class DebtPositionPersonEntityTypeCsvConverter extends AbstractEnumCsvConverter<PersonEntityType> {

  public static final DebtPositionPersonEntityTypeCsvConverter INSTANCE = new DebtPositionPersonEntityTypeCsvConverter();

  private DebtPositionPersonEntityTypeCsvConverter() {
    super(Map.of(), PersonEntityType::fromValue, PersonEntityType.class, "entityType");
  }
}
