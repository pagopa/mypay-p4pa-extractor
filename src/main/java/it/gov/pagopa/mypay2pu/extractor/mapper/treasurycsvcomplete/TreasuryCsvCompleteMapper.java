package it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.dto.export.TreasuryCsvCompleteDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TreasuryCsvCompleteMapper {

  public TreasuryCsvCompleteDTO map(TreasuryCsvComplete treasury) {
    return TreasuryCsvCompleteDTO.builder()
      .annoBolletta(treasury.deAnnoBolletta())
      .codBolletta(treasury.codBolletta())
      .codEnteBT(null)
      .codIstatEnte(null)
      .enteIpaCode(treasury.codIpaEnte())
      .iuf(treasury.codIdUnivocoFlusso())
      .iuv(treasury.codIdUnivocoVersamento())
      .codConto(treasury.codConto())
      .codIdDominio(treasury.codIdDominio())
      .codTipoMovimento(treasury.codTipoMovimento())
      .codCausale(treasury.codCausale())
      .deCausale(treasury.deCausale())
      .importoCentesimi(toBigDecimal(treasury.numIpBolletta()))
      .dataBolletta(toLocalDate(treasury.dtBolletta()))
      .dataRicezione(treasury.dtRicezione())
      .annoDocumento(treasury.deAnnoDocumento())
      .codDocumento(treasury.codDocumento())
      .codBollo(treasury.codBollo())
      .cognome(treasury.deCognome())
      .nome(treasury.deNome())
      .via(treasury.deVia())
      .cap(treasury.deCap())
      .citta(treasury.deCitta())
      .codiceFiscale(treasury.codCodiceFiscale())
      .partitaIva(treasury.codPartitaIva())
      .codAbi(treasury.codAbi())
      .codCab(treasury.codCab())
      .codIban(treasury.codIban())
      .codContoAnagrafica(treasury.codContoAnagrafica())
      .deAeProvvisorio(treasury.deAeProvvisorio())
      .codProvvisorio(treasury.codProvvisorio())
      .codTipoConto(treasury.codTipoConto())
      .codProcesso(treasury.codProcesso())
      .codPgEsecuzione(treasury.codPgEsecuzione())
      .codPgTrasferimento(treasury.codPgTrasferimento())
      .numPgProcesso(toLong(treasury.numPgProcesso()))
      .dataValutaRegione(toLocalDate(treasury.dtDataValutaRegione()))
      .flgRegolarizzata(treasury.flgRegolarizzata())
      .dataEffettivaSospeso(toLocalDate(treasury.dtEffettivaSospeso()))
      .codiceGestionaleProvvisorio(treasury.codiceGestionaleProvvisorio())
      .endToEndId(treasury.endToEndId())
      .build();
  }

  public String buildLogicalKey(TreasuryCsvComplete treasury) {
    return buildLogicalKey(treasury.deAnnoBolletta(), treasury.codBolletta());
  }

  public String buildLogicalKey(String annoBolletta, String codBolletta) {
    return annoBolletta + "|" + codBolletta;
  }

  private BigDecimal toBigDecimal(String value) {
    return value == null ? null : new BigDecimal(value);
  }

  private Long toLong(String value) {
    return value == null ? null : Long.valueOf(value);
  }

  private java.time.LocalDate toLocalDate(java.time.LocalDateTime value) {
    return value == null ? null : value.toLocalDate();
  }
}
