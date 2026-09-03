package it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuTreasuryCsvCompleteDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class TreasuryCsvCompleteMapper {

  public PuTreasuryCsvCompleteDTO map(TreasuryCsvComplete treasury) {
    return PuTreasuryCsvCompleteDTO.builder()
      .billYear(treasury.deAnnoBolletta())
      .billCode(treasury.codBolletta())
      .orgBtCode(null)
      .orgIstatCode(null)
      .organizationIpaCode(treasury.codIpaEnte())
      .iuf(treasury.codIdUnivocoFlusso())
      .iuv(treasury.codIdUnivocoVersamento())
      .accountCode(treasury.codConto())
      .domainIdCode(treasury.codIdDominio())
      .transactionTypeCode(treasury.codTipoMovimento())
      .remittanceCode(treasury.codCausale())
      .remittanceDescription(treasury.deCausale())
      .billAmountCents(toCents(treasury.numIpBolletta()))
      .billDate(toLocalDate(treasury.dtBolletta()))
      .receptionDate(treasury.dtRicezione())
      .documentYear(treasury.deAnnoDocumento())
      .documentCode(treasury.codDocumento())
      .sealCode(treasury.codBollo())
      .pspLastName(treasury.deCognome())
      .pspFirstName(treasury.deNome())
      .pspAddress(treasury.deVia())
      .pspPostalCode(treasury.deCap())
      .pspCity(treasury.deCitta())
      .pspFiscalCode(treasury.codCodiceFiscale())
      .pspVatNumber(treasury.codPartitaIva())
      .abiCode(treasury.codAbi())
      .cabCode(treasury.codCab())
      .ibanCode(treasury.codIban())
      .accountRegistryCode(treasury.codContoAnagrafica())
      .provisionalAe(treasury.deAeProvvisorio())
      .provisionalCode(treasury.codProvvisorio())
      .accountTypeCode(treasury.codTipoConto())
      .processCode(treasury.codProcesso())
      .executionPgCode(treasury.codPgEsecuzione())
      .transferPgCode(treasury.codPgTrasferimento())
      .processPgNumber(treasury.numPgProcesso())
      .regionValueDate(toLocalDate(treasury.dtDataValutaRegione()))
      .isRegularized(treasury.flgRegolarizzata())
      .actualSuspensionDate(toLocalDate(treasury.dtEffettivaSospeso()))
      .managementProvisionalCode(treasury.codiceGestionaleProvvisorio())
      .endToEndCode(treasury.endToEndId())
      .build();
  }

  private Long toCents(BigDecimal value) {
    return value == null ? null : value.movePointRight(2).longValueExact();
  }

  private java.time.LocalDate toLocalDate(LocalDateTime value) {
    return value == null ? null : value.toLocalDate();
  }
}
