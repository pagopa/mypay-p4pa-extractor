package it.gov.pagopa.mypay2pu.extractor.dto;

import java.time.LocalDate;

public record OrganizationDTO(
  String ipaCode,
  String externalOrganizationId,
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
  Boolean flagNotifyOutcomePush
) {
}
