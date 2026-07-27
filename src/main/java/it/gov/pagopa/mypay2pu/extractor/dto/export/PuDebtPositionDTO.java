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

  @CsvBindByName(column = "IUPD", profiles = "V2_0")
  private String iupdOrg;

  @CsvBindByName(column = "descrizionePosizioneDebitoria", profiles = "V2_0")
  private String description;

  @CsvBindByName(column = "dataValidita", profiles = "V2_0")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate validityDate;

  @CsvBindByName(column = "coobbligato", profiles = "V2_0")
  private Boolean multiDebtor;

  @CsvBindByName(column = "dataNotifica", profiles = "V2_0")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate notificationDate;

  @CsvBindByName(column = "indiceOpzionePagamento", profiles = "V2_0")
  @NotNull
  private Integer paymentOptionIndex;

  @CsvBindByName(column = "tipoOpzionePagamento", profiles = "V2_0")
  @NotBlank
  private String paymentOptionType;

  @CsvBindByName(column = "descrizioneOpzionePagamento", profiles = "V2_0")
  private String paymentOptionDescription;

  @CsvBindByName(column = "IUD", profiles = "V2_0")
  @NotBlank
  private String iud;

  @CsvBindByName(column = "codIuv", profiles = "V2_0")
  private String iuv;

  @CsvBindByName(column = "tipoIdentificativoUnivoco", profiles = "V2_0")
  @NotNull
  private PersonEntityType entityType;

  @CsvBindByName(column = "codiceIdentificativoUnivoco", profiles = "V2_0")
  @NotBlank
  private String fiscalCode;

  @CsvBindByName(column = "anagraficaPagatore", profiles = "V2_0")
  @NotBlank
  private String fullName;

  @CsvBindByName(column = "indirizzoPagatore", profiles = "V2_0")
  private String address;

  @CsvBindByName(column = "civicoPagatore", profiles = "V2_0")
  private String civic;

  @CsvBindByName(column = "capPagatore", profiles = "V2_0")
  private String postalCode;

  @CsvBindByName(column = "localitaPagatore", profiles = "V2_0")
  private String location;

  @CsvBindByName(column = "provinciaPagatore", profiles = "V2_0")
  private String province;

  @CsvBindByName(column = "nazionePagatore", profiles = "V2_0")
  private String nation;

  @CsvBindByName(column = "mailPagatore", profiles = "V2_0")
  private String email;

  @CsvBindByName(column = "dataEsecuzionePagamento", profiles = "V2_0")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate dueDate;

  @CsvBindByName(column = "importoDovuto", profiles = "V2_0")
  @NotNull
  private BigDecimal amount;

  @CsvBindByName(column = "tipoDovuto", profiles = "V2_0")
  @NotBlank
  private String debtPositionTypeCode;

  @CsvBindByName(column = "causaleVersamento", profiles = "V2_0")
  @NotBlank
  private String remittanceInformation;

  @CsvBindByName(column = "datiSpecificiRiscossione", profiles = "V2_0")
  private String legacyPaymentMetadata;

  @CsvBindByName(column = "flgGeneraIuv", profiles = "V2_0")
  @NotNull
  private Boolean generateNotice;

  @CsvBindByName(column = "flagPagamentoPu", profiles = "V2_0")
  @NotNull
  private Boolean flagPuPagoPaPayment;

  @CsvBindByName(column = "bilancio", profiles = "V2_0")
  private String balance;

  @CsvBindByName(column = "flagMultiBeneficiario", profiles = "V2_0")
  private Boolean flagMultiBeneficiary;

  @CsvBindByName(column = "numeroBeneficiari", profiles = "V2_0")
  private Integer numberBeneficiary;

  @CsvBindAndJoinByName(column = ".*_1", elementType = String.class, profiles = "V2_0")
  private MultiValuedMap<String, String> transfer1;

  @CsvBindAndJoinByName(column = ".*_2", elementType = String.class, profiles = "V2_0")
  private MultiValuedMap<String, String> transfer2;

  @CsvBindAndJoinByName(column = ".*_3", elementType = String.class, profiles = "V2_0")
  private MultiValuedMap<String, String> transfer3;

  @CsvBindAndJoinByName(column = ".*_4", elementType = String.class, profiles = "V2_0")
  private MultiValuedMap<String, String> transfer4;

  @CsvBindAndJoinByName(column = ".*_5", elementType = String.class, profiles = "V2_0")
  private MultiValuedMap<String, String> transfer5;

  @CsvBindByName(column = "configurazioniEsecuzione", profiles = "V2_0")
  private String executionConfig;

  @CsvBindByName(column = "azione", profiles = "V2_0")
  @NotNull
  private Action action;

  @CsvBindByName(column = "draft", profiles = "V2_0")
  private Boolean draft;
}
