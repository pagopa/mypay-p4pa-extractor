package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class PuDebtPositionDTO implements CsvExportDto {

  public static final String VERSION = "2_0";

  @CsvBindByName(column = "IUPD")
  private String iupdOrg;

  @CsvBindByName(column = "descrizionePosizioneDebitoria")
  private String description;

  @CsvBindByName(column = "dataValidita")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate validityDate;

  @CsvBindByName(column = "coobbligato")
  private Boolean multiDebtor;

  @CsvBindByName(column = "dataNotifica")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate notificationDate;

  @CsvBindByName(column = "indiceOpzionePagamento")
  @NotNull
  private Integer paymentOptionIndex;

  @CsvBindByName(column = "tipoOpzionePagamento")
  @NotBlank
  private String paymentOptionType;

  @CsvBindByName(column = "descrizioneOpzionePagamento")
  private String paymentOptionDescription;

  @CsvBindByName(column = "IUD")
  @NotBlank
  private String iud;

  @CsvBindByName(column = "codIuv")
  private String iuv;

  @CsvBindByName(column = "tipoIdentificativoUnivoco")
  @NotNull
  private PersonEntityType entityType;

  @CsvBindByName(column = "codiceIdentificativoUnivoco")
  @NotBlank
  private String fiscalCode;

  @CsvBindByName(column = "anagraficaPagatore")
  @NotBlank
  private String fullName;

  @CsvBindByName(column = "indirizzoPagatore")
  private String address;

  @CsvBindByName(column = "civicoPagatore")
  private String civic;

  @CsvBindByName(column = "capPagatore")
  private String postalCode;

  @CsvBindByName(column = "localitaPagatore")
  private String location;

  @CsvBindByName(column = "provinciaPagatore")
  private String province;

  @CsvBindByName(column = "nazionePagatore")
  private String nation;

  @CsvBindByName(column = "mailPagatore")
  private String email;

  @CsvBindByName(column = "dataEsecuzionePagamento")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate dueDate;

  @CsvBindByName(column = "importoDovuto")
  @NotNull
  private BigDecimal amount;

  @CsvBindByName(column = "tipoDovuto")
  @NotBlank
  private String debtPositionTypeCode;

  @CsvBindByName(column = "causaleVersamento")
  @NotBlank
  private String remittanceInformation;

  @CsvBindByName(column = "datiSpecificiRiscossione")
  private String legacyPaymentMetadata;

  @CsvBindByName(column = "flgGeneraIuv")
  @NotNull
  private Boolean generateNotice;

  @CsvBindByName(column = "flagPagamentoPu")
  @NotNull
  private Boolean flagPuPagoPaPayment;

  @CsvBindByName(column = "bilancio")
  private String balance;

  @CsvBindByName(column = "flagMultiBeneficiario")
  private Boolean flagMultiBeneficiary;

  @CsvBindByName(column = "numeroBeneficiari")
  private Integer numberBeneficiary;

  @CsvBindAndJoinByName(column = ".*_1", elementType = String.class)
  private MultiValuedMap<String, String> transfer1;

  @CsvBindAndJoinByName(column = ".*_2", elementType = String.class)
  private MultiValuedMap<String, String> transfer2;

  @CsvBindAndJoinByName(column = ".*_3", elementType = String.class)
  private MultiValuedMap<String, String> transfer3;

  @CsvBindAndJoinByName(column = ".*_4", elementType = String.class)
  private MultiValuedMap<String, String> transfer4;

  @CsvBindAndJoinByName(column = ".*_5", elementType = String.class)
  private MultiValuedMap<String, String> transfer5;

  @CsvBindByName(column = "configurazioniEsecuzione")
  private String executionConfig;

  @CsvBindByName(column = "azione")
  @NotNull
  private Action action;

  @CsvBindByName(column = "draft")
  private Boolean draft;
}
