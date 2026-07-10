package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationExportDTO {

  @CsvBindByName(column = "ipa_code")
  @NotBlank
  @Size(max = 256)
  @Pattern(regexp = "^[A-Z0-9_]+$")
  private String ipaCode;

  @CsvBindByName(column = "external_organization_id")
  @Size(max = 256)
  private String externalOrganizationId;

  @CsvBindByName(column = "org_fiscal_code")
  @NotBlank
  @Size(max = 11)
  @Pattern(regexp = "^[0-9]{11}$")
  private String orgFiscalCode;

  @CsvBindByName(column = "org_name")
  @NotBlank
  @Size(max = 256)
  private String orgName;

  @CsvBindByName(column = "org_type_code")
  @Size(max = 35)
  private String orgTypeCode;

  @CsvBindByName(column = "org_email")
  @Email
  @Size(max = 255)
  private String orgEmail;

  @CsvBindByName(column = "iban")
  @Size(max = 35)
  @Pattern(
    regexp = "^$|^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$"
  )
  private String iban;

  @CsvBindByName(column = "postal_iban")
  @Size(max = 35)
  @Pattern(
    regexp = "^$|^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$"
  )
  private String postalIban;

  @CsvBindByName(column = "segregation_code")
  @Size(max = 2)
  @Pattern(
    regexp = "^$|^[A-Za-z0-9]{1,2}$"
  )
  private String segregationCode;

  @CsvBindByName(column = "cbill_inter_bank_code")
  @Size(max = 5)
  @Pattern(
    regexp = "^$|^[0-9]{5}$"
  )
  private String cbillInterBankCode;

  @CsvBindByName(column = "org_logo")
  @Size(max = 255)
  private String orgLogo;

  @CsvBindByName(column = "additional_language")
  @Size(max = 2)
  @Pattern(
    regexp = "^$|^[A-Za-z]{2}$"
  )
  private String additionalLanguage;

  @CsvBindByName(column = "start_date")
  private LocalDate startDate;

  @CsvBindByName(column = "flag_notify_io")
  @NotNull
  private Boolean flagNotifyIo;

  @CsvBindByName(column = "flag_notify_io_bkp")
  private Boolean flagNotifyIoBkp;

  @CsvBindByName(column = "flag_notify_outcome_push")
  @NotNull
  private Boolean flagNotifyOutcomePush;

  @CsvBindByName(column = "status")
  @NotBlank
  @Size(max = 256)
  private String status;

  @CsvBindByName(column = "broker_cf")
  @Size(max = 50)
  @Pattern(
    regexp = "^$|^(?:[0-9]{11})$"
  )
  private String brokerCf;

  @CsvBindByName(column = "io_api_key")
  @Size(max = 255)
  private String ioApiKey;

  @CsvBindByName(column = "flag_treasury")
  @NotBlank
  @Size(max = 5)
  @Pattern(
    regexp = "^(true|false)$"
  )
  private String flagTreasury;

  @CsvBindByName(column = "send_api_key")
  @Size(max = 255)
  private String sendApiKey;

  @CsvBindByName(column = "generate_notice_api_key")
  @Size(max = 255)
  private String generateNoticeApiKey;
}
