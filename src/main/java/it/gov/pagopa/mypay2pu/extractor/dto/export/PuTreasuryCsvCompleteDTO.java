package it.gov.pagopa.mypay2pu.extractor.dto.export;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class PuTreasuryCsvCompleteDTO implements CsvExportDto {

  public static final String VERSION = "1_0";

  @CsvBindByName(column = "annoBolletta")
  private String billYear;
  @CsvBindByName(column = "codBolletta")
  private String billCode;
  @CsvBindByName(column = "codEnteBT")
  private String orgBtCode;
  @CsvBindByName(column = "codIstatEnte")
  private String orgIstatCode;
  @CsvBindByName(column = "enteIpaCode")
  private String organizationIpaCode;
  @CsvBindByName(column = "iuf")
  private String iuf;
  @CsvBindByName(column = "iuv")
  private String iuv;
  @CsvBindByName(column = "codConto")
  private String accountCode;
  @CsvBindByName(column = "codIdDominio")
  private String domainIdCode;
  @CsvBindByName(column = "codTipoMovimento")
  private String transactionTypeCode;
  @CsvBindByName(column = "codCausale")
  private String remittanceCode;
  @CsvBindByName(column = "causale")
  private String remittanceDescription;
  @CsvBindByName(column = "importoCentesimi")
  private Long billAmountCents;
  @CsvBindByName(column = "dataBolletta")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate billDate;
  @CsvBindByName(column = "dataRicezione")
  @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime receptionDate;
  @CsvBindByName(column = "annoDocumento")
  private String documentYear;
  @CsvBindByName(column = "codDocumento")
  private String documentCode;
  @CsvBindByName(column = "codBollo")
  private String sealCode;
  @CsvBindByName(column = "pspCognome")
  private String pspLastName;
  @CsvBindByName(column = "pspNome")
  private String pspFirstName;
  @CsvBindByName(column = "pspIndirizzo")
  private String pspAddress;
  @CsvBindByName(column = "pspCodicePostale")
  private String pspPostalCode;
  @CsvBindByName(column = "pspCitta")
  private String pspCity;
  @CsvBindByName(column = "pspCf")
  private String pspFiscalCode;
  @CsvBindByName(column = "pspPiva")
  private String pspVatNumber;
  @CsvBindByName(column = "codAbi")
  private String abiCode;
  @CsvBindByName(column = "codCab")
  private String cabCode;
  @CsvBindByName(column = "codIban")
  private String ibanCode;
  @CsvBindByName(column = "codContoAnagrafica")
  private String accountRegistryCode;
  @CsvBindByName(column = "aeProvvisorio")
  private String provisionalAe;
  @CsvBindByName(column = "codProvvisorio")
  private String provisionalCode;
  @CsvBindByName(column = "codTipoConto")
  private String accountTypeCode;
  @CsvBindByName(column = "codProcesso")
  private String processCode;
  @CsvBindByName(column = "codPgEsecuzione")
  private String executionPgCode;
  @CsvBindByName(column = "codPgTrasferimento")
  private String transferPgCode;
  @CsvBindByName(column = "numPgProcesso")
  private Long processPgNumber;
  @CsvBindByName(column = "dataValutaRegione")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate regionValueDate;
  @CsvBindByName(column = "flgRegolarizzata")
  private Boolean isRegularized;
  @CsvBindByName(column = "dataEffettivaSospeso")
  @CsvDate(value = "yyyy-MM-dd")
  private LocalDate actualSuspensionDate;
  @CsvBindByName(column = "codGestionaleProvvisorio")
  private String managementProvisionalCode;
  @CsvBindByName(column = "endToEndId")
  private String endToEndCode;
}
