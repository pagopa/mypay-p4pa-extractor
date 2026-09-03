package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TreasuryCsvCompleteDTO implements CsvExportDto {

  @CsvBindByName(column = "annoBolletta")
  private String annoBolletta;
  @CsvBindByName(column = "codBolletta")
  private String codBolletta;
  @CsvBindByName(column = "codEnteBT")
  private String codEnteBT;
  @CsvBindByName(column = "codIstatEnte")
  private String codIstatEnte;
  @CsvBindByName(column = "enteIpaCode")
  private String enteIpaCode;
  @CsvBindByName(column = "iuf")
  private String iuf;
  @CsvBindByName(column = "iuv")
  private String iuv;
  @CsvBindByName(column = "codConto")
  private String codConto;
  @CsvBindByName(column = "codIdDominio")
  private String codIdDominio;
  @CsvBindByName(column = "codTipoMovimento")
  private String codTipoMovimento;
  @CsvBindByName(column = "codCausale")
  private String codCausale;
  @CsvBindByName(column = "deCausale")
  private String deCausale;
  @CsvBindByName(column = "importoCentesimi")
  private BigDecimal importoCentesimi;
  @CsvBindByName(column = "dataBolletta")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate dataBolletta;
  @CsvBindByName(column = "dataRicezione")
  @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime dataRicezione;
  @CsvBindByName(column = "annoDocumento")
  private String annoDocumento;
  @CsvBindByName(column = "codDocumento")
  private String codDocumento;
  @CsvBindByName(column = "codBollo")
  private String codBollo;
  @CsvBindByName(column = "cognome")
  private String cognome;
  @CsvBindByName(column = "nome")
  private String nome;
  @CsvBindByName(column = "via")
  private String via;
  @CsvBindByName(column = "cap")
  private String cap;
  @CsvBindByName(column = "citta")
  private String citta;
  @CsvBindByName(column = "codiceFiscale")
  private String codiceFiscale;
  @CsvBindByName(column = "partitaIva")
  private String partitaIva;
  @CsvBindByName(column = "codAbi")
  private String codAbi;
  @CsvBindByName(column = "codCab")
  private String codCab;
  @CsvBindByName(column = "codIban")
  private String codIban;
  @CsvBindByName(column = "codContoAnagrafica")
  private String codContoAnagrafica;
  @CsvBindByName(column = "deAeProvvisorio")
  private String deAeProvvisorio;
  @CsvBindByName(column = "codProvvisorio")
  private String codProvvisorio;
  @CsvBindByName(column = "codTipoConto")
  private String codTipoConto;
  @CsvBindByName(column = "codProcesso")
  private String codProcesso;
  @CsvBindByName(column = "codPgEsecuzione")
  private String codPgEsecuzione;
  @CsvBindByName(column = "codPgTrasferimento")
  private String codPgTrasferimento;
  @CsvBindByName(column = "numPgProcesso")
  private Long numPgProcesso;
  @CsvBindByName(column = "dataValutaRegione")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate dataValutaRegione;
  @CsvBindByName(column = "flgRegolarizzata")
  private Boolean flgRegolarizzata;
  @CsvBindByName(column = "dataEffettivaSospeso")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate dataEffettivaSospeso;
  @CsvBindByName(column = "codiceGestionaleProvvisorio")
  private String codiceGestionaleProvvisorio;
  @CsvBindByName(column = "endToEndId")
  private String endToEndId;
}
