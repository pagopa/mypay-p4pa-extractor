package it.gov.pagopa.mypay2pu.extractor.mapper.paymentnotification;

import it.gov.pagopa.mypay2pu.extractor.mapper.enums.AbstractEnumCsvConverter;
import it.gov.pagopa.pu.classification.dto.generated.PersonEntityType;

import java.util.Map;

public final class PaymentNotificationPersonEntityTypeCsvConverter extends AbstractEnumCsvConverter<PersonEntityType> {

  public static final PaymentNotificationPersonEntityTypeCsvConverter INSTANCE =
    new PaymentNotificationPersonEntityTypeCsvConverter();

  private PaymentNotificationPersonEntityTypeCsvConverter() {
    super(Map.of(), PersonEntityType::fromValue, PersonEntityType.class, "debtorUniqueIdentifierType");
  }
}
