package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PuDebtPositionTypeDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "brokerCf")
  @NotBlank
  private String brokerCf;

  @CsvBindByName(column = "debtPositionTypeCode")
  @NotBlank
  private String debtPositionTypeCode;

  @CsvBindByName(column = "description")
  @NotBlank
  private String description;

  @CsvBindByName(column = "orgType")
  @NotBlank
  private String orgType;

  @CsvBindByName(column = "macroArea")
  @NotBlank
  private String macroArea;

  @CsvBindByName(column = "serviceType")
  @NotBlank
  private String serviceType;

  @CsvBindByName(column = "collectingReason")
  @NotBlank
  private String collectingReason;

  @CsvBindByName(column = "taxonomyCode")
  @NotBlank
  private String taxonomyCode;

  @CsvBindByName(column = "flagAnonymousFiscalCode")
  @NotNull
  private Boolean flagAnonymousFiscalCode;

  @CsvBindByName(column = "flagMandatoryDueDate")
  @NotNull
  private Boolean flagMandatoryDueDate;

  @CsvBindByName(column = "flagNotifyIo")
  @NotNull
  private Boolean flagNotifyIo;

  @CsvBindByName(column = "ioTemplateMessage")
  private String ioTemplateMessage;

  @CsvBindByName(column = "ioTemplateSubject")
  private String ioTemplateSubject;
}
