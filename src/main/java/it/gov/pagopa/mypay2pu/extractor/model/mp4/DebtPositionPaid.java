package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebtPositionPaid implements ExportModel {

  private String iuf;
  private String codIpaEnte;
  private Long numRigaFlusso;
  private String codIud;
  private String codRpSilinviarpIdUnivocoVersamento;
  private String deEVersioneOggetto;
  private String codEDomIdDominio;
  private String codEDomIdStazioneRichiedente;
  private String codEIdMessaggioRicevuta;
  private LocalDateTime codEDataOraMessaggioRicevuta;
  private String codERiferimentoMessaggioRichiesta;
  private LocalDateTime codERiferimentoDataRichiesta;
  private Character codEIstitAttIdUnivAttTipoIdUnivoco;
  private String codEIstitAttIdUnivAttCodiceIdUnivoco;
  private String deEIstitAttDenominazioneAttestante;
  private String codEIstitAttCodiceUnitOperAttestante;
  private String deEIstitAttDenomUnitOperAttestante;
  private String deEIstitAttIndirizzoAttestante;
  private String deEIstitAttCivicoAttestante;
  private String codEIstitAttCapAttestante;
  private String deEIstitAttLocalitaAttestante;
  private String deEIstitAttProvinciaAttestante;
  private String codEIstitAttNazioneAttestante;
  private Character codEEnteBenefIdUnivBenefTipoIdUnivoco;
  private String codEEnteBenefIdUnivBenefCodiceIdUnivoco;
  private String deEEnteBenefDenominazioneBeneficiario;
  private String codEEnteBenefCodiceUnitOperBeneficiario;
  private String deEEnteBenefDenomUnitOperBeneficiario;
  private String deEEnteBenefIndirizzoBeneficiario;
  private String deEEnteBenefCivicoBeneficiario;
  private String codEEnteBenefCapBeneficiario;
  private String deEEnteBenefLocalitaBeneficiario;
  private String deEEnteBenefProvinciaBeneficiario;
  private String codEEnteBenefNazioneBeneficiario;
  private Character codESoggVersIdUnivVersTipoIdUnivoco;
  private String codESoggVersIdUnivVersCodiceIdUnivoco;
  private String deESoggVersAnagraficaVersante;
  private String deESoggVersIndirizzoVersante;
  private String deESoggVersCivicoVersante;
  private String codESoggVersCapVersante;
  private String deESoggVersLocalitaVersante;
  private String deESoggVersProvinciaVersante;
  private String deESoggVersNazioneVersante;
  private String deESoggVersEmailVersante;
  private Character codESoggPagIdUnivPagTipoIdUnivoco;
  private String codESoggPagIdUnivPagCodiceIdUnivoco;
  private String codESoggPagAnagraficaPagatore;
  private String deESoggPagIndirizzoPagatore;
  private String deESoggPagCivicoPagatore;
  private String codESoggPagCapPagatore;
  private String deESoggPagLocalitaPagatore;
  private String deESoggPagProvinciaPagatore;
  private String deESoggPagNazionePagatore;
  private String deESoggPagEmailPagatore;
  private Character codEDatiPagCodiceEsitoPagamento;
  private BigDecimal numEDatiPagImportoTotalePagato;
  private String codEDatiPagIdUnivocoVersamento;
  private String codEDatiPagCodiceContestoPagamento;
  private BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato;
  private String deEDatiPagDatiSingPagEsitoSingoloPagamento;
  private LocalDateTime dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;
  private String codEDatiPagDatiSingPagIdUnivocoRiscoss;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private String deEDatiPagDatiSingPagDatiSpecificiRiscossione;
  private String codTipoDovuto;
  private String deRtInviartTipoFirma;
  private byte[] blbRtPayload;
  private Integer indiceDatiSingoloPagamento;
  private BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp;
  private String codEDatiPagDatiSingPagAllegatoRicevutaTipo;
  private byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest;
  private String bilancio;
  private String codFiscalePa1;
  private String deNomePa1;
  private String codTassonomicoDovutoPa1;
  private LocalDateTime dtCreazione;

  @Override
  public String logicalKey() {
    return codIud + "|" + codRpSilinviarpIdUnivocoVersamento;
  }
}
