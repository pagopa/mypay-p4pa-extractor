package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record DebtPositionTypeOrg(
  String ipaCode,
  String balance,
  String code,
  String description,
  String iban,
  String postalIban,
  String postalAccountCode,
  String holderPostalCc,
  String orgSector,
  Long amountCents,
  String externalPaymentUrl,
  Boolean flagAnonymousFiscalCode,
  Boolean flagMandatoryDueDate,
  Boolean flagSpontaneous,
  Boolean flagActive,
  Boolean flagNotifyOutcomePush,
  String notifyOutcomePushOrgSilServiceCode,
  Boolean flagAmountActualization,
  String amountActualizationOrgSilServiceCode,
  String spontaneousFormCode
) implements ExportModel {
}
