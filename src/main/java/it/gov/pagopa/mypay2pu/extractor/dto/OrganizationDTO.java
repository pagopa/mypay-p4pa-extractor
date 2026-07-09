package it.gov.pagopa.mypay2pu.extractor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrganizationDTO(
  @NotBlank
  String ipaCode,
  String externalOrganizationId,
  @NotBlank
  String orgFiscalCode,
  @NotBlank
  String orgName,
  String orgTypeCode,
  String orgEmail,
  String iban,
  String postalIban,
  String segregationCode,
  String cbillInterBankCode,
  String orgLogo,
  @NotBlank
  String status,
  String additionalLanguage,
  LocalDate startDate,
  @NotNull
  Boolean flagNotifyIo,
  @NotNull
  Boolean flagNotifyOutcomePush
) {
}
