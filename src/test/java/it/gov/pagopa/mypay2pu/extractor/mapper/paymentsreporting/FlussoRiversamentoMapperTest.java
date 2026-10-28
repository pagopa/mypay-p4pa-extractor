package it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.mypay2pu.extractor.exception.InvalidValueException;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlussoRiversamentoMapperTest {

  private final FlussoRiversamentoMapper mapper = new FlussoRiversamentoMapper();

  @Test
  void givenRowsWhenMapThenBuildFlowAndCalculateAggregates() {
    FlussoRiversamento result = mapper.map(List.of(row(1, "23.00"), row(2, "7.50")));

    assertEquals("FLOW_1", result.getIdentificativoFlusso());
    assertEquals(new BigDecimal("2"), result.getNumeroTotalePagamenti());
    assertEquals(new BigDecimal("30.50"), result.getImportoTotalePagamenti());
    assertEquals(2, result.getDatiSingoliPagamentis().size());
    TestUtils.checkNotNullFields(result, "revisioneFlusso");
    TestUtils.checkNotNullFields(result.getIstitutoMittente());
    TestUtils.checkNotNullFields(result.getIstitutoMittente().getIdentificativoUnivocoMittente());
    TestUtils.checkNotNullFields(result.getIstitutoRicevente());
    TestUtils.checkNotNullFields(result.getIstitutoRicevente().getIdentificativoUnivocoRicevente());
    result.getDatiSingoliPagamentis().forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenRowsWithInconsistentHeaderWhenMapThenFail() {
    PaymentReporting inconsistentRow = new PaymentReporting(
      "FLOW_1",
      "1.1",
      LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30),
      "REG_1",
      LocalDate.of(2026, Month.JANUARY, 15),
      "B",
      "PPAYITR1XXX",
      "Poste Pay",
      "PPAYITR1XXX",
      "G",
      "80050050154",
      "Different organization",
      "IUV_2",
      "IUR_2",
      2,
      new BigDecimal("7.50"),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );

    List<PaymentReporting> rows = List.of(row(1, "23.00"), inconsistentRow);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.map(rows));

    assertInvalidValueException(exception);
  }

  @Test
  void givenRowWithMissingMandatoryHeaderWhenMapThenFail() {
    PaymentReporting rowWithMissingHeader = new PaymentReporting(
      "FLOW_1",
      null,
      LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30),
      "REG_1",
      LocalDate.of(2026, Month.JANUARY, 15),
      "B",
      "PPAYITR1XXX",
      "Poste Pay",
      "PPAYITR1XXX",
      "G",
      "80050050154",
      "Organization",
      "IUV_1",
      "IUR_1",
      1,
      new BigDecimal("23.00"),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );

    List<PaymentReporting> rows = List.of(rowWithMissingHeader);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.map(rows));

    assertInvalidValueException(exception);
  }

  @Test
  void givenEmptyRowsWhenMapThenFail() {
    List<PaymentReporting> rows = List.of();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.map(rows));

    assertInvalidValueException(exception);
    assertNull(exception.getCause().getCause());
  }

  @Test
  void givenRowsWithDuplicatePaymentDetailWhenMapThenFail() {
    List<PaymentReporting> rows = List.of(row(1, "23.00"), row(1, "7.50"));

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> mapper.map(rows)
    );

    assertInvalidValueException(exception);
  }

  @Test
  void givenRowWithMissingMandatoryPaymentDetailWhenMapThenFail() {
    PaymentReporting rowWithMissingDetail = row(
      "IUV_1",
      "IUR_1",
      null,
      new BigDecimal("23.00"),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );
    List<PaymentReporting> rows = List.of(rowWithMissingDetail);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> mapper.map(rows)
    );

    assertInvalidValueException(exception);
  }

  @Test
  void givenRowWithAmountHavingMoreThanTwoDecimalsWhenMapThenFail() {
    List<PaymentReporting> rows = List.of(row(1, "23.001"));

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> mapper.map(rows)
    );

    assertInvalidValueException(exception);
    assertSuppressedCause(exception, ArithmeticException.class);
  }

  @Test
  void givenRowWithInvalidSenderIdentifierTypeWhenMapThenFail() {
    PaymentReporting rowWithInvalidSenderIdentifierType = rowWithIdentifierTypes("INVALID", "G");
    List<PaymentReporting> rows = List.of(rowWithInvalidSenderIdentifierType);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> mapper.map(rows)
    );

    assertInvalidValueException(exception);
    assertSuppressedCause(exception, IllegalArgumentException.class);
  }

  @Test
  void givenRowWithInvalidReceiverIdentifierTypeWhenMapThenFail() {
    PaymentReporting rowWithInvalidReceiverIdentifierType = rowWithIdentifierTypes("B", "INVALID");
    List<PaymentReporting> rows = List.of(rowWithInvalidReceiverIdentifierType);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> mapper.map(rows)
    );

    assertInvalidValueException(exception);
    assertSuppressedCause(exception, IllegalArgumentException.class);
  }

  private PaymentReporting row(int index, String amount) {
    return row(
      "IUV_" + index,
      "IUR_" + index,
      index,
      new BigDecimal(amount),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );
  }

  private PaymentReporting row(
    String iuv,
    String iur,
    Integer index,
    BigDecimal amount,
    String outcome,
    LocalDate outcomeDate
  ) {
    return new PaymentReporting(
      "FLOW_1",
      "1.1",
      LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30),
      "REG_1",
      LocalDate.of(2026, Month.JANUARY, 15),
      "B",
      "PPAYITR1XXX",
      "Poste Pay",
      "PPAYITR1XXX",
      "G",
      "80050050154",
      "Organization",
      iuv,
      iur,
      index,
      amount,
      outcome,
      outcomeDate
    );
  }

  private PaymentReporting rowWithIdentifierTypes(String senderIdentifierType, String receiverIdentifierType) {
    return new PaymentReporting(
      "FLOW_1",
      "1.1",
      LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30),
      "REG_1",
      LocalDate.of(2026, Month.JANUARY, 15),
      senderIdentifierType,
      "PPAYITR1XXX",
      "Poste Pay",
      "PPAYITR1XXX",
      receiverIdentifierType,
      "80050050154",
      "Organization",
      "IUV_1",
      "IUR_1",
      1,
      new BigDecimal("23.00"),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );
  }

  private void assertInvalidValueException(IllegalArgumentException exception) {
    InvalidValueException cause = assertInstanceOf(InvalidValueException.class, exception.getCause());
    assertEquals("INVALID_PAYMENTS_REPORTING_FLOW", cause.getCode());
  }

  private void assertSuppressedCause(IllegalArgumentException exception, Class<? extends Throwable> expectedType) {
    Throwable[] suppressed = exception.getCause().getSuppressed();
    assertEquals(1, suppressed.length);
    assertInstanceOf(expectedType, suppressed[0]);
  }
}
