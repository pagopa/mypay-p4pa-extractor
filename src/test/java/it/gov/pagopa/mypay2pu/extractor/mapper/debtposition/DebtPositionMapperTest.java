package it.gov.pagopa.mypay2pu.extractor.mapper.debtposition;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DebtPositionMapperTest {

  private final DebtPositionMapper debtPositionMapper = new DebtPositionMapper();

  @Test
  void mapShouldPopulateExportDtoUsingActionProvidedByService() {
    DebtPosition debtPosition = new DebtPosition(
      "IUPD-1",
      "description",
      LocalDate.of(2026, Month.JANUARY, 15),
      false,
      LocalDate.of(2026, Month.JANUARY, 16),
      1,
      "SINGLE_INSTALLMENT",
      "Pagamento Singolo Avviso",
      "IUD-1",
      "IUV-1",
      "F",
      "CF123",
      "John Doe",
      "Street",
      "10",
      "00100",
      "Rome",
      "RM",
      "IT",
      "john.doe@example.com",
      LocalDate.of(2026, Month.JANUARY, 20),
      BigDecimal.TEN,
      "TAX",
      "remittance",
      "metadata",
      true,
      "balance",
      false,
      true,
      "CFENTE",
      "Ente",
      "IT60X0542811101000000123456",
      "causale",
      BigDecimal.ONE,
      "9/0101101IM/",
      LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(),
      LocalDate.of(2026, Month.JANUARY, 11).atStartOfDay()
    );

    PuDebtPositionDTO result = debtPositionMapper.map(debtPosition, Action.M);

    assertEquals(debtPosition.iupd(), result.getIupdOrg());
    assertEquals(debtPosition.descrizionePosizioneDebitoria(), result.getDescription());
    assertEquals(debtPosition.dataValidita(), result.getValidityDate());
    assertEquals(debtPosition.coobbligato(), result.getMultiDebtor());
    assertEquals(debtPosition.dataNotifica(), result.getNotificationDate());
    assertEquals(debtPosition.indiceOpzionePagamento(), result.getPaymentOptionIndex());
    assertEquals(debtPosition.tipoOpzionePagamento(), result.getPaymentOptionType());
    assertEquals(debtPosition.descrizioneOpzionePagamento(), result.getPaymentOptionDescription());
    assertEquals(debtPosition.iud(), result.getIud());
    assertEquals(debtPosition.codIuv(), result.getIuv());
    assertEquals(PersonEntityType.F, result.getEntityType());
    assertEquals(debtPosition.codiceIdentificativoUnivoco(), result.getFiscalCode());
    assertEquals(debtPosition.anagraficaPagatore(), result.getFullName());
    assertEquals(debtPosition.indirizzoPagatore(), result.getAddress());
    assertEquals(debtPosition.civicoPagatore(), result.getCivic());
    assertEquals(debtPosition.capPagatore(), result.getPostalCode());
    assertEquals(debtPosition.localitaPagatore(), result.getLocation());
    assertEquals(debtPosition.provinciaPagatore(), result.getProvince());
    assertEquals(debtPosition.nazionePagatore(), result.getNation());
    assertEquals(debtPosition.mailPagatore(), result.getEmail());
    assertEquals(debtPosition.dataEsecuzionePagamento(), result.getDueDate());
    assertEquals(debtPosition.importoDovuto(), result.getAmount());
    assertEquals(debtPosition.tipoDovuto(), result.getDebtPositionTypeCode());
    assertEquals(debtPosition.causaleVersamento(), result.getRemittanceInformation());
    assertEquals(debtPosition.datiSpecificiRiscossione(), result.getLegacyPaymentMetadata());
    assertEquals(debtPosition.flgGeneraIuv(), result.getGenerateNotice());
    assertEquals(Boolean.TRUE, result.getFlagPuPagoPaPayment());
    assertEquals(debtPosition.bilancio(), result.getBalance());
    assertEquals(debtPosition.flagMultiBeneficiario(), result.getFlagMultiBeneficiary());
    assertEquals(1, result.getNumberBeneficiary());
    assertNotNull(result.getTransfer1());
    assertEquals(List.of("CFENTE"), List.copyOf(result.getTransfer1().get("codiceFiscaleEnte_1")));
    assertEquals(List.of("Ente"), List.copyOf(result.getTransfer1().get("denominazioneEnte_1")));
    assertEquals(List.of("IT60X0542811101000000123456"), List.copyOf(result.getTransfer1().get("ibanAccreditoEnte_1")));
    assertEquals(List.of("causale"), List.copyOf(result.getTransfer1().get("causaleVersamentoEnte_1")));
    assertEquals(List.of("1"), List.copyOf(result.getTransfer1().get("importoVersamentoEnte_1")));
    assertEquals(List.of("9/0101101IM/"), List.copyOf(result.getTransfer1().get("codiceTassonomiaEnte_1")));
    assertEquals(Action.M, result.getAction());
    assertEquals(debtPosition.draft(), result.getDraft());
    TestUtils.checkNotNullFields(result, "transfer1", "transfer2", "transfer3", "transfer4", "transfer5", "executionConfig");
  }

  @Test
  void mapShouldPreserveNullOptionalFieldsAndZeroBeneficiaries() {
    DebtPosition debtPosition = new DebtPosition(
      "IUPD-2",
      "description",
      LocalDate.of(2026, Month.FEBRUARY, 15),
      true,
      null,
      1,
      "SINGLE_INSTALLMENT",
      null,
      "IUD-2",
      null,
      "G",
      "CF999",
      "Company",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      BigDecimal.ONE,
      "TAX2",
      "remittance",
      null,
      false,
      null,
      true,
      false,
      null,
      null,
      null,
      null,
      null,
      null,
      LocalDate.of(2026, Month.FEBRUARY, 10).atStartOfDay(),
      null
    );

    PuDebtPositionDTO result = debtPositionMapper.map(debtPosition, Action.I);

    assertEquals(PersonEntityType.G, result.getEntityType());
    assertEquals(0, result.getNumberBeneficiary());
    assertEquals(Action.I, result.getAction());
    assertNull(result.getNotificationDate());
    assertNull(result.getPaymentOptionDescription());
    assertNull(result.getIuv());
    assertNull(result.getAddress());
    assertNull(result.getCivic());
    assertNull(result.getPostalCode());
    assertNull(result.getLocation());
    assertNull(result.getProvince());
    assertNull(result.getNation());
    assertNull(result.getEmail());
    assertNull(result.getDueDate());
    assertNull(result.getLegacyPaymentMetadata());
    assertNull(result.getBalance());
    assertNull(result.getTransfer1());
    assertEquals(Boolean.FALSE, result.getGenerateNotice());
    TestUtils.checkNotNullFields(
      result,
      "notificationDate",
      "paymentOptionDescription",
      "iuv",
      "address",
      "civic",
      "postalCode",
      "location",
      "province",
      "nation",
      "email",
      "dueDate",
      "legacyPaymentMetadata",
      "balance",
      "transfer1",
      "transfer2",
      "transfer3",
      "transfer4",
      "transfer5",
      "executionConfig"
    );
  }

  @Test
  void mapShouldSkipBlankTransferFields() {
    DebtPosition debtPosition = new DebtPosition(
      "IUPD-3",
      "description",
      LocalDate.of(2026, Month.MARCH, 15),
      false,
      null,
      1,
      "SINGLE_INSTALLMENT",
      null,
      "IUD-3",
      "IUV-3",
      "F",
      "CF333",
      "Name",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      BigDecimal.ONE,
      "TAX3",
      "remittance",
      null,
      true,
      null,
      false,
      true,
      "   ",
      "",
      " \t",
      null,
      BigDecimal.TEN,
      " ",
      LocalDate.of(2026, Month.MARCH, 10).atStartOfDay(),
      null
    );

    PuDebtPositionDTO result = debtPositionMapper.map(debtPosition, Action.M);

    assertNotNull(result.getTransfer1());
    assertEquals(List.of("10"), List.copyOf(result.getTransfer1().get("importoVersamentoEnte_1")));
    assertEquals(1, result.getTransfer1().size());
  }
}
