package it.gov.pagopa.mypay2pu.extractor.dto;

import it.gov.pagopa.mypay2pu.extractor.enums.MigrationFileType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequestDTO {
  @NotBlank
  private String ipaCode;

  @NotEmpty
  @Size(min = 1, max = 1, message = "fileTypes must contain exactly one element")
  private List<MigrationFileType> fileTypes;

  @Valid
  private ExtractionFiltersDTO filters;
}
