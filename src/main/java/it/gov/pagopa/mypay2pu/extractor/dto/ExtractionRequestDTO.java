package it.gov.pagopa.mypay2pu.extractor.dto;

import it.gov.pagopa.mypay2pu.extractor.enums.MigrationFileType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequestDTO {
  @NotBlank
  private String ipaCode;

  @NotNull
  private MigrationFileType fileType;

  @Valid
  private ExtractionFiltersDTO filters;
}
