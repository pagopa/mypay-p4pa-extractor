package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuAssessmentsDTO implements CsvExportDto {

    @CsvBindByName(column = "nomeAccertamento")
    @NotBlank
    private String assessmentName;

    @CsvBindByName(column = "enteIpaCode")
    private String organizationIpaCode;

    @CsvBindByName(column = "codiceEnteTipoDovuto")
    @NotBlank
    private String debtPositionTypeOrgCode;

    @CsvBindByName(column = "iuv")
    @NotBlank
    private String iuv;

    @CsvBindByName(column = "iud")
    @NotBlank
    private String iud;

    @CsvBindByName(column = "codUfficio")
    private String officeCode;

    @CsvBindByName(column = "descrizioneCodUfficio")
    private String officeDescription;

    @CsvBindByName(column = "codCapitolo")
    @NotBlank
    private String sectionCode;

    @CsvBindByName(column = "descrizioneCodCapitolo")
    private String sectionDescription;

    @CsvBindByName(column = "codiceAccertamento")
    private String assessmentCode;

    @CsvBindByName(column = "descrizioneCodiceAccertamento")
    private String assessmentDescription;

    @CsvBindByName(column = "importoCentesimi")
    @NotBlank
    private Long amountCents;

    @CsvBindByName(column = "importoVersato")
    @NotNull
    private Boolean amountSubmitted;

}

