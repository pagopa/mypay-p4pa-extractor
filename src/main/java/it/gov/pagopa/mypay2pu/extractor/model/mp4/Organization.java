package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

import java.time.LocalDate;

public record Organization(
  String ipaCode,
  String orgFiscalCode,
  String orgName,
  String orgTypeCode,
  String orgEmail,
  String iban,
  String postalIban,
  String segregationCode,
  String cbillInterBankCode,
  String orgLogo,
  String status,
  String additionalLanguage,
  LocalDate startDate,
  Boolean flagNotifyIo,
  Boolean flagNotifyIoBkp,
  Boolean flagNotifyOutcomePush,
  String ioApiKey

) implements ExportModel {

  @Override
  public String logicalKey() {
    return ipaCode;
  }
}
