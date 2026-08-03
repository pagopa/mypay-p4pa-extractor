package it.gov.pagopa.mypay2pu.extractor.mapper.paymentnotification;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuPaymentNotificationDTO;
import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.PaymentNotification;
import it.gov.pagopa.pu.classification.dto.generated.PersonEntityType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentNotificationMapperTest {

  private final PaymentNotificationMapper paymentNotificationMapper = new PaymentNotificationMapper();

  @Test
  void mapShouldPopulateExportDto() {
    PaymentNotification paymentNotification = paymentNotification("F");

    PuPaymentNotificationDTO result = paymentNotificationMapper.map(paymentNotification);

    assertEquals(paymentNotification.iud(), result.getIud());
    assertEquals(paymentNotification.iuv(), result.getIuv());
    assertEquals(PersonEntityType.F, result.getDebtorUniqueIdentifierType());
    assertEquals(paymentNotification.codiceIdentificativoUnivoco(), result.getDebtorUniqueIdentifierCode());
    assertEquals(paymentNotification.anagraficaPagatore(), result.getDebtorFullName());
    assertEquals(paymentNotification.indirizzoPagatore(), result.getDebtorAddress());
    assertEquals(paymentNotification.civicoPagatore(), result.getDebtorCivic());
    assertEquals(paymentNotification.capPagatore(), result.getDebtorPostalCode());
    assertEquals(paymentNotification.localitaPagatore(), result.getDebtorLocation());
    assertEquals(paymentNotification.provinciaPagatore(), result.getDebtorProvince());
    assertEquals(paymentNotification.nazionePagatore(), result.getDebtorNation());
    assertEquals(paymentNotification.emailPagatore(), result.getDebtorEmail());
    assertEquals(paymentNotification.dataEsecuzionePagamento(), result.getPaymentExecutionDate());
    assertEquals(paymentNotification.importoDovutoPagato(), result.getAmountPaid());
    assertEquals(paymentNotification.commissioneCaricoPa(), result.getPaCommission());
    assertEquals(paymentNotification.tipoDovuto(), result.getDebtPositionTypeOrgCode());
    assertEquals(paymentNotification.tipoVersamento(), result.getPaymentType());
    assertEquals(paymentNotification.causaleVersamento(), result.getRemittanceInformation());
    assertEquals(paymentNotification.datiSpecificiRiscossione(), result.getTransferCategory());
    assertEquals(paymentNotification.bilancio(), result.getBalance());
  }

  @Test
  void mapShouldPreserveNullOptionalFields() {
    PaymentNotification paymentNotification = new PaymentNotification(
      "IUD-1", "IUV-1", "G", "CF123", "John Doe", null, null, null, null, null, null, null,
      LocalDate.of(2026, Month.AUGUST, 3), BigDecimal.TEN, BigDecimal.ZERO, "TAX", "PAGOPA", "remittance",
      "9/0101101IM/", null, "IPA", LocalDateTime.now(), null
    );

    PuPaymentNotificationDTO result = paymentNotificationMapper.map(paymentNotification);

    assertEquals(PersonEntityType.G, result.getDebtorUniqueIdentifierType());
    assertNull(result.getDebtorAddress());
    assertNull(result.getDebtorCivic());
    assertNull(result.getDebtorPostalCode());
    assertNull(result.getDebtorLocation());
    assertNull(result.getDebtorProvince());
    assertNull(result.getDebtorNation());
    assertNull(result.getDebtorEmail());
    assertNull(result.getBalance());
  }

  @Test
  void mapShouldRejectUnknownDebtorUniqueIdentifierType() {
    PaymentNotification paymentNotification = paymentNotification("X");

    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> paymentNotificationMapper.map(paymentNotification)
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("debtorUniqueIdentifierType", exception.getField());
    assertEquals("X", exception.getRejectedValue());
  }

  private PaymentNotification paymentNotification(String personEntityType) {
    return new PaymentNotification(
      "IUD-1", "IUV-1", personEntityType, "CF123", "John Doe", "Street", "10", "00100", "Rome", "RM",
      "IT", "john.doe@example.com", LocalDate.of(2026, Month.AUGUST, 3), BigDecimal.TEN, BigDecimal.ONE, "TAX",
      "PAGOPA", "remittance", "9/0101101IM/", "balance", "IPA", LocalDateTime.now(), LocalDateTime.now()
    );
  }
}
