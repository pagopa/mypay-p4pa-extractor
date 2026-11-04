package it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumConverter;

import java.util.Map;

public final class PaymentReportingSenderIdentifierTypeConverter
  extends AbstractEnumConverter<StTipoIdentificativoUnivoco> {

  public static final PaymentReportingSenderIdentifierTypeConverter INSTANCE =
    new PaymentReportingSenderIdentifierTypeConverter();

  private PaymentReportingSenderIdentifierTypeConverter() {
    super(Map.of(), StTipoIdentificativoUnivoco::valueOf);
  }

  public StTipoIdentificativoUnivoco toXmlValue(String sourceValue) {
    return convert(sourceValue);
  }
}
