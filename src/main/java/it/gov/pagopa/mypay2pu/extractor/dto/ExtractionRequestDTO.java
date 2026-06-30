package it.gov.pagopa.mypay2pu.extractor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequestDTO {
  @NotBlank
  private String ipaCode;

  @NotBlank
  private String fileTypes;

  @Valid
  private ExtractionFiltersDTO filters;
}
