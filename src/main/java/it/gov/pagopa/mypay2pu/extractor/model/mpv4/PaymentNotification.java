package it.gov.pagopa.mypay2pu.extractor.model.mpv4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentNotification(
  String iud,
  String iuv,
  String tipoIdentificativoUnivoco,
  String codiceIdentificativoUnivoco,
  String anagraficaPagatore,
  String indirizzoPagatore,
  String civicoPagatore,
  String capPagatore,
  String localitaPagatore,
  String provinciaPagatore,
  String nazionePagatore,
  String emailPagatore,
  LocalDate dataEsecuzionePagamento,
  BigDecimal importoDovutoPagato,
  BigDecimal commissioneCaricoPa,
  String tipoDovuto,
  String tipoVersamento,
  String causaleVersamento,
  String datiSpecificiRiscossione,
  String bilancio,
  String ipaCode,
  LocalDateTime dtCreazione,
  LocalDateTime dtUltimaModifica
) implements ExportModel {

  @Override
  public String logicalKey() {
    return iud;
  }
}
