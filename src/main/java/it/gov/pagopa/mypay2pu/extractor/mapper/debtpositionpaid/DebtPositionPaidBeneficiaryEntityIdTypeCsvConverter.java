package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionpaid;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionPaidDTO.EntityIdType;
import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;

import java.util.Map;

public final class DebtPositionPaidBeneficiaryEntityIdTypeCsvConverter
  extends AbstractEnumCsvConverter<EntityIdType> {

  public static final DebtPositionPaidBeneficiaryEntityIdTypeCsvConverter INSTANCE =
    new DebtPositionPaidBeneficiaryEntityIdTypeCsvConverter();

  private DebtPositionPaidBeneficiaryEntityIdTypeCsvConverter() {
    super(Map.of("G", EntityIdType.G), EntityIdType.class, "enteBenefTipoIdentificativoUnivoco");
  }
}
