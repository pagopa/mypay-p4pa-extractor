package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import it.gov.pagopa.pu.classification.dto.generated.PersonEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class PuPaymentNotificationDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "IUD")
  @NotBlank
  private String iud;

  @CsvBindByName(column = "codIuv")
  @NotBlank
  private String iuv;

  @CsvBindByName(column = "tipoIdentificativoUnivoco")
  @NotNull
  private PersonEntityType debtorUniqueIdentifierType;

  @CsvBindByName(column = "codiceIdentificativoUnivoco")
  @NotBlank
  private String debtorUniqueIdentifierCode;

  @CsvBindByName(column = "anagraficaPagatore")
  @NotBlank
  private String debtorFullName;

  @CsvBindByName(column = "indirizzoPagatore")
  private String debtorAddress;

  @CsvBindByName(column = "civicoPagatore")
  private String debtorCivic;

  @CsvBindByName(column = "capPagatore")
  private String debtorPostalCode;

  @CsvBindByName(column = "localitaPagatore")
  private String debtorLocation;

  @CsvBindByName(column = "provinciaPagatore")
  private String debtorProvince;

  @CsvBindByName(column = "nazionePagatore")
  private String debtorNation;

  @CsvBindByName(column = "e-mailPagatore")
  private String debtorEmail;

  @CsvBindByName(column = "dataEsecuzionePagamento")
  @CsvDate(value = "yyyy-MM-dd")
  @NotNull
  private LocalDate paymentExecutionDate;

  @CsvBindByName(column = "importoDovutoPagato")
  @NotNull
  private BigDecimal amountPaid;

  @CsvBindByName(column = "commissioneCaricoPa")
  @NotNull
  private BigDecimal paCommission;

  @CsvBindByName(column = "tipoDovuto")
  @NotBlank
  private String debtPositionTypeOrgCode;

  @CsvBindByName(column = "tipoVersamento")
  @NotBlank
  private String paymentType;

  @CsvBindByName(column = "causaleVersamento")
  @NotBlank
  private String remittanceInformation;

  @CsvBindByName(column = "datiSpecificiRiscossione")
  @NotBlank
  private String transferCategory;

  @CsvBindByName(column = "bilancio")
  private String balance;
}
