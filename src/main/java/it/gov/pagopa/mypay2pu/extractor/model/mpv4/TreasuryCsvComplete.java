package it.gov.pagopa.mypay2pu.extractor.model.mpv4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TreasuryCsvComplete(
  String deAnnoBolletta,
  String codBolletta,
  String codEnteBt,
  String codIstatEnte,
  String codIpaEnte,
  String codIdUnivocoFlusso,
  String codIdUnivocoVersamento,
  String codConto,
  String codIdDominio,
  String codTipoMovimento,
  String codCausale,
  String deCausale,
  BigDecimal numIpBolletta,
  LocalDateTime dtBolletta,
  LocalDateTime dtRicezione,
  String deAnnoDocumento,
  String codDocumento,
  String codBollo,
  String deCognome,
  String deNome,
  String deVia,
  String deCap,
  String deCitta,
  String codCodiceFiscale,
  String codPartitaIva,
  String codAbi,
  String codCab,
  String codIban,
  String codContoAnagrafica,
  String deAeProvvisorio,
  String codProvvisorio,
  String codTipoConto,
  String codProcesso,
  String codPgEsecuzione,
  String codPgTrasferimento,
  Long numPgProcesso,
  LocalDateTime dtDataValutaRegione,
  Boolean flgRegolarizzata,
  LocalDateTime dtEffettivaSospeso,
  String codiceGestionaleProvvisorio,
  String endToEndId
) implements ExportModel {

  @Override
  public String logicalKey() {
    return deAnnoBolletta + "|" + codBolletta;
  }
}
