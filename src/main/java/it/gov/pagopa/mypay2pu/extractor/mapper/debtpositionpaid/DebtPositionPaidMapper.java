package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionpaid;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionPaidDTO;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtposition.DebtPositionPersonEntityTypeCsvConverter;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class DebtPositionPaidMapper {

  private static final Set<String> SUPPORTED_VERSIONS = Set.of(
    PuDebtPositionPaidDTO.V1_0,
    PuDebtPositionPaidDTO.V1_1,
    PuDebtPositionPaidDTO.V1_2,
    PuDebtPositionPaidDTO.V1_3
  );

  public PuDebtPositionPaidDTO map(DebtPositionPaid debtPositionPaid, String version) {
    if (!SUPPORTED_VERSIONS.contains(version)) {
      throw new IllegalArgumentException("Unsupported DEBT_POSITIONS_PAID version: " + version);
    }

    boolean receiptFieldsSupported = PuDebtPositionPaidDTO.V1_3.equals(version);
    return PuDebtPositionPaidDTO.builder()
      .iuf(debtPositionPaid.getIuf())
      .numRigaFlusso(debtPositionPaid.getNumRigaFlusso() == null ? null : Math.toIntExact(debtPositionPaid.getNumRigaFlusso()))
      .codIud(debtPositionPaid.getCodIud())
      .codIuv(debtPositionPaid.getCodRpSilinviarpIdUnivocoVersamento())
      .versioneOggetto(debtPositionPaid.getDeEVersioneOggetto())
      .identificativoDominio(debtPositionPaid.getCodEDomIdDominio())
      .identificativoStazioneRichiedente(debtPositionPaid.getCodEDomIdStazioneRichiedente())
      .identificativoMessaggioRicevuta(debtPositionPaid.getCodEIdMessaggioRicevuta())
      .dataOraMessaggioRicevuta(debtPositionPaid.getDtEDataOraMessaggioRicevuta())
      .riferimentoMessaggioRichiesta(debtPositionPaid.getCodERiferimentoMessaggioRichiesta())
      .riferimentoDataRichiesta(toLocalDate(debtPositionPaid.getCodERiferimentoDataRichiesta()))
      .tipoIdentificativoUnivocoAttestante(toString(debtPositionPaid.getCodEIstitAttIdUnivAttTipoIdUnivoco()))
      .codiceIdentificativoUnivocoAttestante(debtPositionPaid.getCodEIstitAttIdUnivAttCodiceIdUnivoco())
      .denominazioneAttestante(debtPositionPaid.getDeEIstitAttDenominazioneAttestante())
      .codiceUnitOperAttestante(debtPositionPaid.getCodEIstitAttCodiceUnitOperAttestante())
      .denomUnitOperAttestante(debtPositionPaid.getDeEIstitAttDenomUnitOperAttestante())
      .indirizzoAttestante(debtPositionPaid.getDeEIstitAttIndirizzoAttestante())
      .civicoAttestante(debtPositionPaid.getDeEIstitAttCivicoAttestante())
      .capAttestante(debtPositionPaid.getCodEIstitAttCapAttestante())
      .localitaAttestante(debtPositionPaid.getDeEIstitAttLocalitaAttestante())
      .provinciaAttestante(debtPositionPaid.getDeEIstitAttProvinciaAttestante())
      .nazioneAttestante(debtPositionPaid.getCodEIstitAttNazioneAttestante())
      .enteBenefTipoIdentificativoUnivoco(toEntityIdType(debtPositionPaid.getCodEEnteBenefIdUnivBenefTipoIdUnivoco()))
      .enteBenefCodiceIdentificativoUnivoco(debtPositionPaid.getCodEEnteBenefIdUnivBenefCodiceIdUnivoco())
      .denominazioneBeneficiario(debtPositionPaid.getDeEEnteBenefDenominazioneBeneficiario())
      .codiceUnitOperBeneficiario(debtPositionPaid.getCodEEnteBenefCodiceUnitOperBeneficiario())
      .denomUnitOperBeneficiario(debtPositionPaid.getDeEEnteBenefDenomUnitOperBeneficiario())
      .indirizzoBeneficiario(debtPositionPaid.getDeEEnteBenefIndirizzoBeneficiario())
      .civicoBeneficiario(debtPositionPaid.getDeEEnteBenefCivicoBeneficiario())
      .capBeneficiario(debtPositionPaid.getCodEEnteBenefCapBeneficiario())
      .localitaBeneficiario(debtPositionPaid.getDeEEnteBenefLocalitaBeneficiario())
      .provinciaBeneficiario(debtPositionPaid.getDeEEnteBenefProvinciaBeneficiario())
      .nazioneBeneficiario(debtPositionPaid.getCodEEnteBenefNazioneBeneficiario())
      .soggVersTipoIdentificativoUnivoco(
        DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue(toString(debtPositionPaid.getCodESoggVersIdUnivVersTipoIdUnivoco()))
      )
      .soggVersCodiceIdentificativoUnivoco(debtPositionPaid.getCodESoggVersIdUnivVersCodiceIdUnivoco())
      .anagraficaVersante(debtPositionPaid.getDeESoggVersAnagraficaVersante())
      .indirizzoVersante(debtPositionPaid.getDeESoggVersIndirizzoVersante())
      .civicoVersante(debtPositionPaid.getDeESoggVersCivicoVersante())
      .capVersante(debtPositionPaid.getCodESoggVersCapVersante())
      .localitaVersante(debtPositionPaid.getDeESoggVersLocalitaVersante())
      .provinciaVersante(debtPositionPaid.getDeESoggVersProvinciaVersante())
      .nazioneVersante(debtPositionPaid.getDeESoggVersNazioneVersante())
      .emailVersante(debtPositionPaid.getDeESoggVersEmailVersante())
      .soggPagTipoIdentificativoUnivoco(
        DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue(toString(debtPositionPaid.getCodESoggPagIdUnivPagTipoIdUnivoco()))
      )
      .soggPagCodiceIdentificativoUnivoco(debtPositionPaid.getCodESoggPagIdUnivPagCodiceIdUnivoco())
      .anagraficaPagatore(debtPositionPaid.getCodESoggPagAnagraficaPagatore())
      .indirizzoPagatore(debtPositionPaid.getDeESoggPagIndirizzoPagatore())
      .civicoPagatore(debtPositionPaid.getDeESoggPagCivicoPagatore())
      .capPagatore(debtPositionPaid.getCodESoggPagCapPagatore())
      .localitaPagatore(debtPositionPaid.getDeESoggPagLocalitaPagatore())
      .provinciaPagatore(debtPositionPaid.getDeESoggPagProvinciaPagatore())
      .nazionePagatore(debtPositionPaid.getDeESoggPagNazionePagatore())
      .emailPagatore(debtPositionPaid.getDeESoggPagEmailPagatore())
      .codiceEsitoPagamento(toString(debtPositionPaid.getCodEDatiPagCodiceEsitoPagamento()))
      .importoTotalePagato(debtPositionPaid.getNumEDatiPagImportoTotalePagato())
      .identificativoUnivocoVersamento(debtPositionPaid.getCodEDatiPagIdUnivocoVersamento())
      .codiceContestoPagamento(debtPositionPaid.getCodEDatiPagCodiceContestoPagamento())
      .singoloImportoPagato(debtPositionPaid.getNumEDatiPagDatiSingPagSingoloImportoPagato())
      .esitoSingoloPagamento(debtPositionPaid.getDeEDatiPagDatiSingPagEsitoSingoloPagamento())
      .dataEsitoSingoloPagamento(toLocalDate(debtPositionPaid.getDtEDatiPagDatiSingPagDataEsitoSingoloPagamento()))
      .identificativoUnivocoRiscoss(debtPositionPaid.getCodEDatiPagDatiSingPagIdUnivocoRiscoss())
      .causaleVersamento(debtPositionPaid.getDeRpDatiVersDatiSingVersCausaleVersamento())
      .datiSpecificiRiscossione(debtPositionPaid.getDeEDatiPagDatiSingPagDatiSpecificiRiscossione())
      .tipoDovuto(debtPositionPaid.getCodTipoDovuto())
      .tipoFirma(debtPositionPaid.getDeRtInviartTipoFirma())
      .rt(toUtf8String(debtPositionPaid.getBlbRtPayload()))
      .indiceDatiSingoloPagamento(debtPositionPaid.getIndiceDatiSingoloPagamento())
      .commissioniApplicatePsp(debtPositionPaid.getNumEDatiPagDatiSingPagCommissioniApplicatePsp())
      .allegatoRicevutaTipo(debtPositionPaid.getCodEDatiPagDatiSingPagAllegatoRicevutaTipo())
      .allegatoRicevutaTest(toUtf8String(debtPositionPaid.getBlbEDatiPagDatiSingPagAllegatoRicevutaTest()))
      .bilancio(debtPositionPaid.getBilancio())
      .codFiscalePa1(receiptFieldsSupported ? debtPositionPaid.getCodFiscalePa1() : null)
      .deNomePa1(receiptFieldsSupported ? debtPositionPaid.getDeNomePa1() : null)
      .codTassonomicoDovutoPa1(receiptFieldsSupported ? debtPositionPaid.getCodTassonomicoDovutoPa1() : null)
      .build();
  }

  private String toString(Character value) {
    return value == null ? null : value.toString();
  }

  private PuDebtPositionPaidDTO.EntityIdType toEntityIdType(Character value) {
    return value == null ? null : PuDebtPositionPaidDTO.EntityIdType.valueOf(value.toString());
  }

  private java.time.LocalDate toLocalDate(java.time.LocalDateTime value) {
    return value == null ? null : value.toLocalDate();
  }

  private String toUtf8String(byte[] value) {
    return value == null ? null : new String(value, StandardCharsets.UTF_8);
  }
}
