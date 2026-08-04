package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvRawColumns;
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
@CsvRawColumns("strutturaPagamentoSpontaneo")
public class PuDebtPositionTypeOrgDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "enteIpaCode")
  @NotBlank
  private String ipaCode;

  @CsvBindByName(column = "bilancioDefault")
  private String balance;

  @CsvBindByName(column = "codice")
  @NotBlank
  private String code;

  @CsvBindByName(column = "descrizione")
  @NotBlank
  private String description;

  @CsvBindByName(column = "codIban")
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

  @CsvBindByName(column = "codiceFiscaleAnonimo")
  @NotNull
  private Boolean flagAnonymousFiscalCode;

  @CsvBindByName(column = "scadenzaObbligatoria")
  @NotNull
  private Boolean flagMandatoryDueDate;

  @CsvBindByName(column = "pagamentoSpontaneo")
  @NotNull
  private Boolean flagSpontaneous;

  @CsvBindByName(column = "notificaIo")
  @NotNull
  private Boolean flagNotifyIo;

  @CsvBindByName(column = "notificaIoBkp")
  @NotNull
  private Boolean flagNotifyIoBkp;

  @CsvBindByName(column = "templateMessaggioIo")
  private String ioTemplateMessage;

  @CsvBindByName(column = "attivo")
  @NotNull
  private Boolean flagActive;

  @CsvBindByName(column = "notificaEsitoPush")
  @NotNull
  private Boolean flagNotifyOutcomePush;

  @CsvBindByName(column = "codServNotificaEsitoPush")
  private String notifyOutcomePushOrgSilServiceCode;

  @CsvBindByName(column = "attualizzazioneImporto")
  @NotNull
  private Boolean flagAmountActualization;

  @CsvBindByName(column = "codServAttualizzazioneImporto")
  private String amountActualizationOrgSilServiceCode;

  @CsvBindByName(column = "esterno")
  @NotNull
  private Boolean flagExternal;

  @CsvBindByName(column = "codiceServizio")
  private String serviceCode;

  @CsvBindByName(column = "templateOggettoIo")
  private String ioTemplateSubject;
}
