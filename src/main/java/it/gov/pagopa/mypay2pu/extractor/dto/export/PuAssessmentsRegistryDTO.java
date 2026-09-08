package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PuAssessmentsRegistryDTO implements CsvExportDto {
  public static final String VERSION = "1.0";

  @CsvBindByName(column = "enteIpaCode")
  private String organizationIpaCode;

  @CsvBindByName(column = "codiceTipoDovuto")
  @NotBlank
  private String debtPositionTypeOrgCode;

  @CsvBindByName(column = "codCapitolo")
  @NotBlank
  private String sectionCode;

  @CsvBindByName(column = "descrizioneCapitolo")
  private String sectionDescription;

  @CsvBindByName(column = "codUfficio")
  private String officeCode;

  @CsvBindByName(column = "descrizioneUfficio")
  private String officeDescription;

  @CsvBindByName(column = "codAccertamento")
  private String assessmentCode;

  @CsvBindByName(column = "descrizioneAccertamento")
  private String assessmentDescription;

  @CsvBindByName(column = "annoEsercizio")
  @NotBlank
  private String operatingYear;

  @CsvBindByName(column = "statoAccertamento")
  @NotBlank
  private String status;
}
