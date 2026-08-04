package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontypeorg;

import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeOrgDao;
import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeOrgDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
import it.gov.pagopa.mypay2pu.extractor.connector.mydictionary.MyDictionaryClient;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgMapperTest {

  private static final String RAW_TEMPLATE_MESSAGE = "Pagamento {desc_pagamento} di {importo} per {anag_pagatore} entro {data_scadenza}";
  private static final String RAW_TEMPLATE_SUBJECT = "Oggetto {desc_pagamento}";

  @Mock
  private DebtPositionTypeOrgDao debtPositionTypeOrgDaoMock;
  @Mock
  private MyDictionaryClient myDictionaryClientMock;

  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgMapper = new DebtPositionTypeOrgMapper(
      debtPositionTypeOrgDaoMock,
      new MyPayProperties(RAW_TEMPLATE_SUBJECT, RAW_TEMPLATE_MESSAGE),
      myDictionaryClientMock
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(debtPositionTypeOrgDaoMock, myDictionaryClientMock);
  }

  @Test
  void mapShouldPopulateExportDtoWithEnrichmentInputs() {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg(
      "IPA1", "BILANCIO", "TAX", "Tax", "IT60X0542811101000000123456",
      "IT60X0542811101000000123456", "123456", "Municipality", "Public administration", 1234L,
      "https://example.test/pay", false, true, true, true, true,
      "PAYMENT_NOTIFICATION", true, "https://example.test/pnd", "SPONT_FORM", "SVC_CODE", false, true
    );
    String strutturaPagamentoSpontaneo = "{\"field\":\"value\"}";

    when(myDictionaryClientMock.getSpontaneousFormStructure(debtPositionTypeOrg.spontaneousFormCode()))
      .thenReturn(strutturaPagamentoSpontaneo);
    when(debtPositionTypeOrgDaoMock.isExternal(debtPositionTypeOrg.ipaCode(), debtPositionTypeOrg.code()))
      .thenReturn(true);

    PuDebtPositionTypeOrgDTO result = debtPositionTypeOrgMapper.map(debtPositionTypeOrg);

    assertEquals(debtPositionTypeOrg.ipaCode(), result.getIpaCode());
    assertEquals(debtPositionTypeOrg.balance(), result.getBalance());
    assertEquals(debtPositionTypeOrg.code(), result.getCode());
    assertEquals(debtPositionTypeOrg.description(), result.getDescription());
    assertEquals(debtPositionTypeOrg.iban(), result.getIban());
    assertEquals(debtPositionTypeOrg.postalIban(), result.getPostalIban());
    assertEquals(debtPositionTypeOrg.postalAccountCode(), result.getPostalAccountCode());
    assertEquals(debtPositionTypeOrg.holderPostalCc(), result.getHolderPostalCc());
    assertEquals(debtPositionTypeOrg.orgSector(), result.getOrgSector());
    assertEquals(strutturaPagamentoSpontaneo, result.getSpontaneousFormStructure());
    assertEquals(debtPositionTypeOrg.amountCents(), result.getAmountCents());
    assertEquals(debtPositionTypeOrg.externalPaymentUrl(), result.getExternalPaymentUrl());
    assertEquals(debtPositionTypeOrg.flagAnonymousFiscalCode(), result.getFlagAnonymousFiscalCode());
    assertEquals(debtPositionTypeOrg.flagMandatoryDueDate(), result.getFlagMandatoryDueDate());
    assertEquals(debtPositionTypeOrg.flagSpontaneous(), result.getFlagSpontaneous());
    assertEquals(false, result.getFlagNotifyIo());
    assertEquals(true, result.getFlagNotifyIoBkp());
    assertEquals(
      "Pagamento %posizioneDebitoria_descrizione% di %importoTotale% per %debitore_nomeCompleto% entro %dataScadenza%",
      result.getIoTemplateMessage()
    );
    assertEquals(debtPositionTypeOrg.flagActive(), result.getFlagActive());
    assertEquals(debtPositionTypeOrg.flagNotifyOutcomePush(), result.getFlagNotifyOutcomePush());
    assertEquals(debtPositionTypeOrg.notifyOutcomePushOrgSilServiceCode(), result.getNotifyOutcomePushOrgSilServiceCode());
    assertEquals(debtPositionTypeOrg.flagAmountActualization(), result.getFlagAmountActualization());
    assertEquals("https://example.test/pnd", result.getAmountActualizationOrgSilServiceCode());
    assertEquals(true, result.getFlagExternal());
    assertEquals("SVC_CODE", result.getServiceCode());
    assertEquals("Oggetto %posizioneDebitoria_descrizione%", result.getIoTemplateSubject());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void mapShouldPreserveNullOptionalFields() {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg(
      "IPA1", null, "FEE", "Fee", "IT60X0542811101000000123456",
      null, null, null, null, null, null, false, false, false, true, false,
      null, false, null, null, null, false, false
    );

    when(debtPositionTypeOrgDaoMock.isExternal(debtPositionTypeOrg.ipaCode(), debtPositionTypeOrg.code()))
      .thenReturn(false);

    PuDebtPositionTypeOrgDTO result = debtPositionTypeOrgMapper.map(debtPositionTypeOrg);

    assertEquals("IPA1", result.getIpaCode());
    assertEquals("FEE", result.getCode());
    assertEquals("Fee", result.getDescription());
    assertEquals("IT60X0542811101000000123456", result.getIban());
    assertEquals(false, result.getFlagAnonymousFiscalCode());
    assertEquals(false, result.getFlagMandatoryDueDate());
    assertEquals(false, result.getFlagSpontaneous());
    assertEquals(false, result.getFlagNotifyIo());
    assertEquals(false, result.getFlagNotifyIoBkp());
    assertEquals(true, result.getFlagActive());
    assertEquals(false, result.getFlagNotifyOutcomePush());
    assertEquals(false, result.getFlagAmountActualization());
    assertEquals(false, result.getFlagExternal());
    assertNull(result.getBalance());
    assertNull(result.getPostalIban());
    assertNull(result.getPostalAccountCode());
    assertNull(result.getHolderPostalCc());
    assertNull(result.getOrgSector());
    assertNull(result.getAmountCents());
    assertNull(result.getExternalPaymentUrl());
    assertNull(result.getNotifyOutcomePushOrgSilServiceCode());
    assertNull( result.getSpontaneousFormCode());
    assertNull(result.getSpontaneousFormStructure());
    assertNull(result.getAmountActualizationOrgSilServiceCode());
    assertNull(result.getServiceCode());
    assertEquals(
      "Pagamento %posizioneDebitoria_descrizione% di %importoTotale% per %debitore_nomeCompleto% entro %dataScadenza%",
      result.getIoTemplateMessage()
    );
    assertEquals("Oggetto %posizioneDebitoria_descrizione%", result.getIoTemplateSubject());

    verify(myDictionaryClientMock, never()).getSpontaneousFormStructure(null);

    TestUtils.checkNotNullFields(
      result,
      "balance",
      "postalIban",
      "postalAccountCode",
      "holderPostalCc",
      "orgSector",
      "spontaneousFormCode",
      "spontaneousFormStructure",
      "amountCents",
      "externalPaymentUrl",
      "notifyOutcomePushOrgSilServiceCode",
      "amountActualizationOrgSilServiceCode",
      "serviceCode"
    );
  }

  @ParameterizedTest
  @MethodSource("httpErrorStatuses")
  void mapShouldKeepRowWhenMyDictionaryReturnsHttpErrorAndFlagSpontaneousIsFalse(HttpStatusCodeException httpException) {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg(
      "IPA1", "BILANCIO", "TAX", "Tax", "IT60X0542811101000000123456",
      null, null, null, null, null, null, false, true, false, true, false,
      null, false, null, "SPONT_FORM", null, false, false
    );
    when(myDictionaryClientMock.getSpontaneousFormStructure("SPONT_FORM")).thenThrow(httpException);
    when(debtPositionTypeOrgDaoMock.isExternal(debtPositionTypeOrg.ipaCode(), debtPositionTypeOrg.code()))
      .thenReturn(false);

    PuDebtPositionTypeOrgDTO result = debtPositionTypeOrgMapper.map(debtPositionTypeOrg);

    assertNull(result.getSpontaneousFormStructure());
    assertEquals(false, result.getFlagSpontaneous());
  }

  @ParameterizedTest
  @MethodSource("httpErrorStatuses")
  void mapShouldDiscardRowWhenMyDictionaryReturnsHttpErrorAndFlagSpontaneousIsTrue(HttpStatusCodeException httpException) {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg(
      "IPA1", "BILANCIO", "TAX", "Tax", "IT60X0542811101000000123456",
      null, null, null, null, null, null, false, true, true, true, false,
      null, false, null, "SPONT_FORM", null, false, false
    );
    when(myDictionaryClientMock.getSpontaneousFormStructure("SPONT_FORM")).thenThrow(httpException);

    assertThrows(CsvRowMappingException.class, () -> debtPositionTypeOrgMapper.map(debtPositionTypeOrg));
    verify(debtPositionTypeOrgDaoMock, never()).isExternal(debtPositionTypeOrg.ipaCode(), debtPositionTypeOrg.code());
  }

  static java.util.stream.Stream<HttpStatusCodeException> httpErrorStatuses() {
    return java.util.stream.Stream.of(
      new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null),
      new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], null),
      new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], null)
    );
  }
}
