package it.gov.pagopa.mypay2pu.extractor.mapper.debtposition;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionMapper {

  public PuDebtPositionDTO map(DebtPosition debtPosition, Action action) {
    return PuDebtPositionDTO.builder()
      .iupdIUD(debtPosition.iud())
      .iupdOrg(debtPosition.iud())
      .iupdPagoPa(debtPosition.iupd())
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
      .generateNotice(Boolean.TRUE.equals(debtPosition.flgGeneraIuv()))
      .flagPuPagoPaPayment(Boolean.FALSE)
      .balance(debtPosition.bilancio())
      .flagMultiBeneficiary(debtPosition.flagMultiBeneficiario())
      .numberBeneficiary(debtPosition.flagMultiBeneficiario() != null && debtPosition.flagMultiBeneficiario() ? 2 : 1)
      .transfer1(buildTransfer1(debtPosition))
      .action(action)
      .draft(debtPosition.draft())
      .build();
  }

  private MultiValuedMap<String, String> buildTransfer1(DebtPosition debtPosition) {
    MultiValuedMap<String, String> transfer1 = new ArrayListValuedHashMap<>();
    addIfHasText(transfer1, "codiceFiscaleEnte1", debtPosition.codiceFiscaleEnte1());
    addIfHasText(transfer1, "denominazioneEnte1", debtPosition.denominazioneEnte1());
    addIfHasText(transfer1, "ibanAccreditoEnte1", debtPosition.ibanAccreditoEnte1());
    addIfHasText(transfer1, "causaleVersamentoEnte1", debtPosition.causaleVersamentoEnte1());
    addIfHasText(transfer1, "codiceTassonomiaEnte1", debtPosition.codiceTassonomiaEnte1());

    if (debtPosition.importoVersamentoEnte1() != null) {
      transfer1.put("importoVersamentoEnte1", debtPosition.importoVersamentoEnte1().toPlainString());
    }
    return transfer1.isEmpty() ? null : transfer1;
  }

  private void addIfHasText(MultiValuedMap<String, String> map, String key, String value) {
    if (StringUtils.isNotBlank(value)) {
      map.put(key, value);
    }
  }
}
