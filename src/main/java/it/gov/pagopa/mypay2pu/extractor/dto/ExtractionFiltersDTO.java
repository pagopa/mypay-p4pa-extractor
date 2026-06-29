package it.gov.pagopa.mypay2pu.extractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionFiltersDTO {
  private LocalDate modifiedFrom;
  private LocalDate modifiedTo;
}
