package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class PuDebtPositionTypeOrgDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "enteIpaCode", required = true)
  @NotBlank
  private String ipaCode;

  @CsvBindByName(column = "bilancioDefault")
  private String balance;

  @CsvBindByName(column = "codice", required = true)
  @NotBlank
  private String code;

  @CsvBindByName(column = "descrizione", required = true)
  @NotBlank
  private String description;

  @CsvBindByName(column = "codIban", required = true)
  @NotBlank
  private String iban;

  @CsvBindByName(column = "ibanPostale")
  private String postalIban;

  @CsvBindByName(column = "codiceContoPostale")
  private String postalAccountCode;

  @CsvBindByName(column = "intestatarioContoPostale")
  private String holderPostalCc;

  @CsvBindByName(column = "settoreOrganizzazione")
  private String orgSector;

  @CsvBindByName(column = "codicePagamentoSpontaneo")
  private String spontaneousFormCode;

  @CsvBindByName(column = "strutturaPagamentoSpontaneo")
  private String spontaneousFormStructure;

  @CsvBindByName(column = "importoCentesimi")
  private Long amountCents;

  @CsvBindByName(column = "urlPagamentoEsterno")
  private String externalPaymentUrl;

  @CsvBindByName(column = "codiceFiscaleAnonimo", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagAnonymousFiscalCode;

  @CsvBindByName(column = "scadenzaObbligatoria", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagMandatoryDueDate;

  @CsvBindByName(column = "pagamentoSpontaneo", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagSpontaneous;

  @CsvBindByName(column = "notificaIo", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagNotifyIo;

  @CsvBindByName(column = "templateMessaggioIo")
  private String ioTemplateMessage;

  @CsvBindByName(column = "attivo", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagActive;

  @CsvBindByName(column = "notificaEsitoPush", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagNotifyOutcomePush;

  @CsvBindByName(column = "codServNotificaEsitoPush")
  private String notifyOutcomePushOrgSilServiceCode;

  @CsvBindByName(column = "attualizzazioneImporto", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagAmountActualization;

  @CsvBindByName(column = "codServAttualizzazioneImporto")
  private String amountActualizationOrgSilServiceCode;

  @CsvBindByName(column = "esterno", required = true)
  @NotBlank
  @Size(max = 5)
  @Pattern(regexp = "^(true|false)$")
  private String flagExternal;

  @CsvBindByName(column = "codiceServizio")
  private String serviceCode;

  @CsvBindByName(column = "templateOggettoIo")
  private String ioTemplateSubject;
}
