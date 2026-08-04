package it.gov.pagopa.mypay2pu.extractor.mapper.paymentnotification;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuPaymentNotificationDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.PaymentNotification;
import org.springframework.stereotype.Component;

@Component
public class PaymentNotificationMapper {

  public PuPaymentNotificationDTO map(PaymentNotification paymentNotification) {
    return PuPaymentNotificationDTO.builder()
      .iud(paymentNotification.iud())
      .iuv(paymentNotification.iuv())
      .debtorUniqueIdentifierType(
        PaymentNotificationPersonEntityTypeCsvConverter.INSTANCE.toCsvValue(paymentNotification.tipoIdentificativoUnivoco())
      )
      .debtorUniqueIdentifierCode(paymentNotification.codiceIdentificativoUnivoco())
      .debtorFullName(paymentNotification.anagraficaPagatore())
      .debtorAddress(paymentNotification.indirizzoPagatore())
      .debtorCivic(paymentNotification.civicoPagatore())
      .debtorPostalCode(paymentNotification.capPagatore())
      .debtorLocation(paymentNotification.localitaPagatore())
      .debtorProvince(paymentNotification.provinciaPagatore())
      .debtorNation(paymentNotification.nazionePagatore())
      .debtorEmail(paymentNotification.emailPagatore())
      .paymentExecutionDate(paymentNotification.dataEsecuzionePagamento())
      .amountPaid(paymentNotification.importoDovutoPagato())
      .paCommission(paymentNotification.commissioneCaricoPa())
      .debtPositionTypeOrgCode(paymentNotification.tipoDovuto())
      .paymentType(paymentNotification.tipoVersamento())
      .remittanceInformation(paymentNotification.causaleVersamento())
      .transferCategory(paymentNotification.datiSpecificiRiscossione())
      .balance(paymentNotification.bilancio())
      .build();
  }
}
