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

  @CsvBindByName(column = "codIpaEnte")
  @NotBlank
  @Size(max = 256)
  @Pattern(regexp = "^[A-Z0-9_]+$")
  private String ipaCode;

  @CsvBindByName(column = "nomeApplicazione")
  @NotBlank
  @Size(max = 255)
  private String applicationName;

  @CsvBindByName(column = "tipoServizio")
  @NotBlank
  @Size(max = 30)
  private String serviceType;

  @CsvBindByName(column = "urlServizio")
  @Size(max = 255)
  private String serviceUrl;

  @CsvBindByName(column = "flagLegacy")
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagLegacy;

  @CsvBindByName(column = "legacyJwtKid")
  @Size(max = 50)
  private String legacyJwtKid;

  @CsvBindByName(column = "legacyJwtSubject")
  @Size(max = 255)
  private String legacyJwtSubject;

  @CsvBindByName(column = "legacyJwtIssuer")
  @Size(max = 50)
  private String legacyJwtIssuer;

  @CsvBindByName(column = "legacyJwtAlgorithm")
  @Size(max = 10)
  private String legacyJwtAlgorithm;

  @CsvBindByName(column = "legacyJwtSigningKey")
  @Size(max = 255)
  private String legacyJwtSigningKey;

  @CsvBindByName(column = "legacyBasicAuthUrl")
  @Size(max = 255)
  private String legacyBasicAuthUrl;

  @CsvBindByName(column = "legacyBasicUser")
  @Size(max = 255)
  private String legacyBasicUser;

  @CsvBindByName(column = "legacyBasicPsw")
  @Size(max = 255)
  private String legacyBasicPsw;
}
