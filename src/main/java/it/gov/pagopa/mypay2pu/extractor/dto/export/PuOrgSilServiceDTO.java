package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"legacyJwtSigningKey", "legacyBasicPsw"})
public class PuOrgSilServiceDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "ipa_code")
  @NotBlank
  @Size(max = 256)
  @Pattern(regexp = "^[A-Z0-9_]+$")
  private String ipaCode;

  @CsvBindByName(column = "application_name")
  @NotBlank
  @Size(max = 255)
  private String applicationName;

  @CsvBindByName(column = "service_type")
  @NotBlank
  @Size(max = 30)
  private String serviceType;

  @CsvBindByName(column = "service_url")
  @Size(max = 255)
  private String serviceUrl;

  @CsvBindByName(column = "flag_legacy")
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagLegacy;

  @CsvBindByName(column = "legacy_jwt_kid")
  @Size(max = 50)
  private String legacyJwtKid;

  @CsvBindByName(column = "legacy_jwt_subject")
  @Size(max = 255)
  private String legacyJwtSubject;

  @CsvBindByName(column = "legacy_jwt_issuer")
  @Size(max = 50)
  private String legacyJwtIssuer;

  @CsvBindByName(column = "legacy_jwt_algorithm")
  @Size(max = 10)
  private String legacyJwtAlgorithm;

  @CsvBindByName(column = "legacy_jwt_signing_key")
  @Size(max = 255)
  private String legacyJwtSigningKey;

  @CsvBindByName(column = "legacy_basic_auth_url")
  @Size(max = 255)
  private String legacyBasicAuthUrl;

  @CsvBindByName(column = "legacy_basic_user")
  @Size(max = 255)
  private String legacyBasicUser;

  @CsvBindByName(column = "legacy_basic_psw")
  @Size(max = 255)
  private String legacyBasicPsw;
}
