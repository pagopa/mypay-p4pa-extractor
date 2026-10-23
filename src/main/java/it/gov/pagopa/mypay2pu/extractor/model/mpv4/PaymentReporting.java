package it.gov.pagopa.mypay2pu.extractor.model.mpv4;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentReporting(
  String codIdentificativoFlusso,
  String versioneOggetto,
  LocalDateTime dtDataOraFlusso,
  String codIdentificativoUnivocoRegolamento,
  LocalDate dtDataRegolamento,
  String codIstMittIdUnivMittTipoIdentificativoUnivoco,
  String codIstMittIdUnivMittCodiceIdentificativoUnivoco,
  String deIstMittDenominazioneMittente,
  String codiceBicBancaDiRiversamento,
  String codIstRicevIdUnivRicevTipoIdentificativoUnivoco,
  String codIstRicevIdUnivRicevCodiceIdentificativoUnivoco,
  String deIstRicevDenominazioneRicevente,
  String codDatiSingPagamIdentificativoUnivocoVersamento,
  String codDatiSingPagamIdentificativoUnivocoRiscossione,
  Integer indiceDatiSingoloPagamento,
  BigDecimal numDatiSingPagamSingoloImportoPagato,
  String codDatiSingPagamCodiceEsitoSingoloPagamento,
  LocalDate dtDatiSingPagamDataEsitoSingoloPagamento
) {
}
