package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DebtPosition(
  String iupd,
  String descrizionePosizioneDebitoria,
  LocalDate dataValidita,
  Boolean coobbligato,
  LocalDate dataNotifica,
  Integer indiceOpzionePagamento,
  String tipoOpzionePagamento,
  String descrizioneOpzionePagamento,
  String iud,
  String codIuv,
  String tipoIdentificativoUnivoco,
  String codiceIdentificativoUnivoco,
  String anagraficaPagatore,
  String indirizzoPagatore,
  String civicoPagatore,
  String capPagatore,
  String localitaPagatore,
  String provinciaPagatore,
  String nazionePagatore,
  String mailPagatore,
  LocalDate dataEsecuzionePagamento,
  BigDecimal importoDovuto,
  String tipoDovuto,
  String causaleVersamento,
  String datiSpecificiRiscossione,
  Boolean flgGeneraIuv,
  String bilancio,
  Boolean draft,
  Boolean flagMultiBeneficiario,
  String codiceFiscaleEnte1,
  String denominazioneEnte1,
  String ibanAccreditoEnte1,
  String causaleVersamentoEnte1,
  BigDecimal importoVersamentoEnte1,
  String codiceTassonomiaEnte1,
  LocalDateTime dtCreazione,
  LocalDateTime dtUltimaModifica
) implements ExportModel {

  @Override
  public String logicalKey() {
    return codIuv;
  }
}
