package it.gov.pagopa.mypay2pu.extractor.dto.export;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PuDebtPositionTypeOrgDTO implements CsvExportDto {

  public static final String VERSION = "1_0";
}
