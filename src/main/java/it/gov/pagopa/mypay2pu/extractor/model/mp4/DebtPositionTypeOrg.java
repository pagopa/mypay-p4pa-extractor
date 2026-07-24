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
  String flagAnonymousFiscalCode,
  String flagMandatoryDueDate,
  String flagSpontaneous,
  String flagNotifyIo,
  String flagActive,
  String flagNotifyOutcomePush,
  String notifyOutcomePushOrgSilServiceCode,
  String flagAmountActualization,
  String serviceCode,
  String spontaneousFormCode,
  String authUrl,
  String userPnd,
  String pswPnd
) implements ExportModel {
}
