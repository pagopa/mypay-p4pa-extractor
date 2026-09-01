package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

public record DebtPositionPaid(
  String iuf,
  String codIpaEnte,
  Long numRigaFlusso,
  String codIud,
  String codRpSilinviarpIdUnivocoVersamento,
  String deEVersioneOggetto,
  String codEDomIdDominio,
  String codEDomIdStazioneRichiedente,
  String codEIdMessaggioRicevuta,
  LocalDateTime dtEDataOraMessaggioRicevuta,
  String codERiferimentoMessaggioRichiesta,
  LocalDateTime codERiferimentoDataRichiesta,
  Character codEIstitAttIdUnivAttTipoIdUnivoco,
  String codEIstitAttIdUnivAttCodiceIdUnivoco,
  String deEIstitAttDenominazioneAttestante,
  String codEIstitAttCodiceUnitOperAttestante,
  String deEIstitAttDenomUnitOperAttestante,
  String deEIstitAttIndirizzoAttestante,
  String deEIstitAttCivicoAttestante,
  String codEIstitAttCapAttestante,
  String deEIstitAttLocalitaAttestante,
  String deEIstitAttProvinciaAttestante,
  String codEIstitAttNazioneAttestante,
  Character codEEnteBenefIdUnivBenefTipoIdUnivoco,
  String codEEnteBenefIdUnivBenefCodiceIdUnivoco,
  String deEEnteBenefDenominazioneBeneficiario,
  String codEEnteBenefCodiceUnitOperBeneficiario,
  String deEEnteBenefDenomUnitOperBeneficiario,
  String deEEnteBenefIndirizzoBeneficiario,
  String deEEnteBenefCivicoBeneficiario,
  String codEEnteBenefCapBeneficiario,
  String deEEnteBenefLocalitaBeneficiario,
  String deEEnteBenefProvinciaBeneficiario,
  String codEEnteBenefNazioneBeneficiario,
  Character codESoggVersIdUnivVersTipoIdUnivoco,
  String codESoggVersIdUnivVersCodiceIdUnivoco,
  String deESoggVersAnagraficaVersante,
  String deESoggVersIndirizzoVersante,
  String deESoggVersCivicoVersante,
  String codESoggVersCapVersante,
  String deESoggVersLocalitaVersante,
  String deESoggVersProvinciaVersante,
  String codESoggVersNazioneVersante,
  String deESoggVersEmailVersante,
  Character codESoggPagIdUnivPagTipoIdUnivoco,
  String codESoggPagIdUnivPagCodiceIdUnivoco,
  String codESoggPagAnagraficaPagatore,
  String deESoggPagIndirizzoPagatore,
  String deESoggPagCivicoPagatore,
  String codESoggPagCapPagatore,
  String deESoggPagLocalitaPagatore,
  String deESoggPagProvinciaPagatore,
  String deESoggPagNazionePagatore,
  String deESoggPagEmailPagatore,
  Character codEDatiPagCodiceEsitoPagamento,
  BigDecimal numEDatiPagImportoTotalePagato,
  String codEDatiPagIdUnivocoVersamento,
  String codEDatiPagCodiceContestoPagamento,
  BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato,
  String deEDatiPagDatiSingPagEsitoSingoloPagamento,
  LocalDateTime dtEDatiPagDatiSingPagDataEsitoSingoloPagamento,
  String codEDatiPagDatiSingPagIdUnivocoRiscoss,
  String deRpDatiVersDatiSingVersCausaleVersamento,
  String deEDatiPagDatiSingPagDatiSpecificiRiscossione,
  String codTipoDovuto,
  String deRtInviartTipoFirma,
  byte[] blbRtPayload,
  Integer indiceDatiSingoloPagamento,
  BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp,
  String codEDatiPagDatiSingPagAllegatoRicevutaTipo,
  byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest,
  String bilancio,
  String codFiscalePa1,
  String deNomePa1,
  String codTassonomicoDovutoPa1,
  LocalDateTime dtCreazione
) implements ExportModel {

  @Override
  public String logicalKey() {
    return codIud + "|" + codRpSilinviarpIdUnivocoVersamento;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    return object instanceof DebtPositionPaid other
      && Arrays.deepEquals(values(), other.values());
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(values());
  }

  @Override
  @NonNull
  public String toString() {
    return "DebtPositionPaid" + Arrays.deepToString(values());
  }

  private Object[] values() {
    return new Object[] {
      iuf, codIpaEnte, numRigaFlusso, codIud, codRpSilinviarpIdUnivocoVersamento,
      deEVersioneOggetto, codEDomIdDominio, codEDomIdStazioneRichiedente,
      codEIdMessaggioRicevuta, dtEDataOraMessaggioRicevuta, codERiferimentoMessaggioRichiesta,
      codERiferimentoDataRichiesta, codEIstitAttIdUnivAttTipoIdUnivoco,
      codEIstitAttIdUnivAttCodiceIdUnivoco, deEIstitAttDenominazioneAttestante,
      codEIstitAttCodiceUnitOperAttestante, deEIstitAttDenomUnitOperAttestante,
      deEIstitAttIndirizzoAttestante, deEIstitAttCivicoAttestante, codEIstitAttCapAttestante,
      deEIstitAttLocalitaAttestante, deEIstitAttProvinciaAttestante, codEIstitAttNazioneAttestante,
      codEEnteBenefIdUnivBenefTipoIdUnivoco, codEEnteBenefIdUnivBenefCodiceIdUnivoco,
      deEEnteBenefDenominazioneBeneficiario, codEEnteBenefCodiceUnitOperBeneficiario,
      deEEnteBenefDenomUnitOperBeneficiario, deEEnteBenefIndirizzoBeneficiario,
      deEEnteBenefCivicoBeneficiario, codEEnteBenefCapBeneficiario,
      deEEnteBenefLocalitaBeneficiario, deEEnteBenefProvinciaBeneficiario,
      codEEnteBenefNazioneBeneficiario, codESoggVersIdUnivVersTipoIdUnivoco,
      codESoggVersIdUnivVersCodiceIdUnivoco, deESoggVersAnagraficaVersante,
      deESoggVersIndirizzoVersante, deESoggVersCivicoVersante, codESoggVersCapVersante,
      deESoggVersLocalitaVersante, deESoggVersProvinciaVersante, codESoggVersNazioneVersante,
      deESoggVersEmailVersante, codESoggPagIdUnivPagTipoIdUnivoco,
      codESoggPagIdUnivPagCodiceIdUnivoco, codESoggPagAnagraficaPagatore,
      deESoggPagIndirizzoPagatore, deESoggPagCivicoPagatore, codESoggPagCapPagatore,
      deESoggPagLocalitaPagatore, deESoggPagProvinciaPagatore, deESoggPagNazionePagatore,
      deESoggPagEmailPagatore, codEDatiPagCodiceEsitoPagamento, numEDatiPagImportoTotalePagato,
      codEDatiPagIdUnivocoVersamento, codEDatiPagCodiceContestoPagamento,
      numEDatiPagDatiSingPagSingoloImportoPagato, deEDatiPagDatiSingPagEsitoSingoloPagamento,
      dtEDatiPagDatiSingPagDataEsitoSingoloPagamento, codEDatiPagDatiSingPagIdUnivocoRiscoss,
      deRpDatiVersDatiSingVersCausaleVersamento, deEDatiPagDatiSingPagDatiSpecificiRiscossione,
      codTipoDovuto, deRtInviartTipoFirma, blbRtPayload, indiceDatiSingoloPagamento,
      numEDatiPagDatiSingPagCommissioniApplicatePsp, codEDatiPagDatiSingPagAllegatoRicevutaTipo,
      blbEDatiPagDatiSingPagAllegatoRicevutaTest, bilancio, codFiscalePa1, deNomePa1,
      codTassonomicoDovutoPa1, dtCreazione
    };
  }
}
