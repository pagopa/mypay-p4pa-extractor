package it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumConverter;

import java.util.Map;

public final class PaymentReportingReceiverIdentifierTypeConverter
  extends AbstractEnumConverter<StTipoIdentificativoUnivocoPersG> {

  public static final PaymentReportingReceiverIdentifierTypeConverter INSTANCE =
    new PaymentReportingReceiverIdentifierTypeConverter();

  private PaymentReportingReceiverIdentifierTypeConverter() {
    super(Map.of(), StTipoIdentificativoUnivocoPersG::valueOf);
  }

  public StTipoIdentificativoUnivocoPersG toXmlValue(String sourceValue) {
    return convert(sourceValue);
  }
}
