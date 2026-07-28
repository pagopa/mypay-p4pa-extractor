package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PuDebtPositionsTypeOrgOperatorsDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "enteIpaCode")
  @NotBlank
  private String organizationIpaCode;

  @CsvBindByName(column = "cfOperatore")
  @NotBlank
  private String operatorFiscalCode;

  @CsvBindByName(column = "codiceTipoDovuto")
  @NotBlank
  private String debtPositionsTypeOrgCode;
}
