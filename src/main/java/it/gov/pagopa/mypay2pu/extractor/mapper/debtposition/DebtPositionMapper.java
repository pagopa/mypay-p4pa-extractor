package it.gov.pagopa.mypay2pu.extractor.mapper.debtposition;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionMapper {

  public PuDebtPositionDTO map(DebtPosition debtPosition, Action action) {
    return PuDebtPositionDTO.builder()
      .iupdOrg(debtPosition.iupd())
      .description(debtPosition.descrizionePosizioneDebitoria())
      .validityDate(debtPosition.dataValidita())
      .multiDebtor(debtPosition.coobbligato())
      .notificationDate(debtPosition.dataNotifica())
      .paymentOptionIndex(debtPosition.indiceOpzionePagamento())
      .paymentOptionType(debtPosition.tipoOpzionePagamento())
      .paymentOptionDescription(debtPosition.descrizioneOpzionePagamento())
      .iud(debtPosition.iud())
      .iuv(debtPosition.codIuv())
      .entityType(DebtPositionPersonEntityTypeCsvConverter.INSTANCE.toCsvValue(debtPosition.tipoIdentificativoUnivoco()))
      .fiscalCode(debtPosition.codiceIdentificativoUnivoco())
      .fullName(debtPosition.anagraficaPagatore())
      .address(debtPosition.indirizzoPagatore())
      .civic(debtPosition.civicoPagatore())
      .postalCode(debtPosition.capPagatore())
      .location(debtPosition.localitaPagatore())
      .province(debtPosition.provinciaPagatore())
      .nation(debtPosition.nazionePagatore())
      .email(debtPosition.mailPagatore())
      .dueDate(debtPosition.dataEsecuzionePagamento())
      .amount(debtPosition.importoDovuto())
      .debtPositionTypeCode(debtPosition.tipoDovuto())
      .remittanceInformation(debtPosition.causaleVersamento())
      .legacyPaymentMetadata(debtPosition.datiSpecificiRiscossione())
      .generateNotice(debtPosition.flgGeneraIuv())
      .flagPuPagoPaPayment(debtPosition.flagPagamentoPu())
      .balance(debtPosition.bilancio())
      .flagMultiBeneficiary(debtPosition.flagMultiBeneficiario())
      .numberBeneficiary(Boolean.TRUE.equals(debtPosition.flagMultiBeneficiario()) ? 1 : 0)
      .action(action)
      .draft(debtPosition.draft())
      .build();
  }
}
