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

  private PaymentReporting row(int index, String amount) {
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
      "IUV_" + index,
      "IUR_" + index,
      index,
      new BigDecimal(amount),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );
  }

  private void assertInvalidValueException(IllegalArgumentException exception) {
    InvalidValueException cause = assertInstanceOf(InvalidValueException.class, exception.getCause());
    assertEquals("INVALID_PAYMENTS_REPORTING_FLOW", cause.getCode());
  }
}
