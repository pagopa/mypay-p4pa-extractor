package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionpaid;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionPaidDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionPaidDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionpaid.DebtPositionPaidMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.ZipUtils;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionPaidExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private DebtPositionPaidDao debtPositionPaidDaoMock;
  @Mock
  private DebtPositionPaidMapper debtPositionPaidMapperMock;

  private DebtPositionPaidExportProcessingService service;
  private ValidatorFactory validatorFactory;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    validatorFactory = Validation.buildDefaultValidatorFactory();
    service = new DebtPositionPaidExportProcessingService(
      debtPositionPaidDaoMock,
      debtPositionPaidMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      validatorFactory.getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(debtPositionPaidDaoMock, debtPositionPaidMapperMock);
    validatorFactory.close();
  }

  @Test
  void whenCompleteMultiOrganizationExtractionThenCreateBrokerIpaNamedZipWithOneCsvPerOrganization() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA1", "IPA2"), MigrationFileType.DEBT_POSITIONS_PAID
    );
    DebtPositionPaid first = debtPositionPaid("IUD-1", "IUV-1");
    DebtPositionPaid second = debtPositionPaid("IUD-2", "IUV-2");

    when(debtPositionPaidDaoMock.findByFilters("IPA1", List.of(), List.of(), null, null, 2, 0))
      .thenReturn(List.of(first));
    when(debtPositionPaidDaoMock.findByFilters("IPA2", List.of(), List.of(), null, null, 2, 0))
      .thenReturn(List.of(second));
    when(debtPositionPaidMapperMock.map(first)).thenReturn(validDto("IUD-1", "IUV-1"));
    when(debtPositionPaidMapperMock.map(second)).thenReturn(validDto("IUD-2", "IUV-2"));

    ExportFileResult result = service.executeExport("extraction-id", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    String zipFileName = result.files().getFirst();
    assertTrue(zipFileName.matches("BROKER_IPA-DEBT_POSITIONS_PAID-\\d{14}-1_0\\.zip"));
    List<String> entries = ZipUtils.readZipEntries(tempDir.resolve("extraction-id").resolve(zipFileName));
    assertEquals(2, entries.size());
    assertTrue(entries.get(0).matches("IPA1-DEBT_POSITIONS_PAID-\\d{14}-1_0\\.csv"));
    assertTrue(entries.get(1).matches("IPA2-DEBT_POSITIONS_PAID-\\d{14}-1_0\\.csv"));
  }

  @Test
  void whenIncrementalExtractionThenPassCreatedIntervalToDao() {
    OffsetDateTime createdFrom = LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0).atOffset(ZoneOffset.UTC);
    OffsetDateTime createdTo = LocalDateTime.of(2026, Month.JANUARY, 11, 10, 0).atOffset(ZoneOffset.UTC);
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA1"),
      MigrationFileType.DEBT_POSITIONS_PAID,
      null,
      new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo)
    );

    when(debtPositionPaidDaoMock.findByFilters("IPA1", List.of(), List.of(), createdFrom, createdTo, 2, 0))
      .thenReturn(List.of());

    assertEquals(List.of(), service.retrieveData("IPA1", request, 2, 0));
  }

  @Test
  void whenLogicalKeyExtractionThenParseIudAndIuvBeforeCallingDao() {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA1"),
      MigrationFileType.DEBT_POSITIONS_PAID,
      null,
      new ExtractionFilters().logicalKey("IUD-1,IUD-2|IUV-1,IUV-2")
    );

    when(debtPositionPaidDaoMock.findByFilters(
      "IPA1", List.of("IUD-1", "IUD-2"), List.of("IUV-1", "IUV-2"), null, null, 2, 0
    )).thenReturn(List.of());

    assertEquals(List.of(), service.retrieveData("IPA1", request, 2, 0));
  }

  @Test
  void whenRowsExceedConfiguredLimitThenSplitCsvFilesAndArchiveThem() throws Exception {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA1"), MigrationFileType.DEBT_POSITIONS_PAID);
    DebtPositionPaid first = debtPositionPaid("IUD-1", "IUV-1");
    DebtPositionPaid second = debtPositionPaid("IUD-2", "IUV-2");
    DebtPositionPaid third = debtPositionPaid("IUD-3", "IUV-3");
    when(debtPositionPaidDaoMock.findByFilters("IPA1", List.of(), List.of(), null, null, 2, 0))
      .thenReturn(List.of(first, second));
    when(debtPositionPaidDaoMock.findByFilters("IPA1", List.of(), List.of(), null, null, 2, 2))
      .thenReturn(List.of(third));
    when(debtPositionPaidMapperMock.map(first)).thenReturn(validDto("IUD-1", "IUV-1"));
    when(debtPositionPaidMapperMock.map(second)).thenReturn(validDto("IUD-2", "IUV-2"));
    when(debtPositionPaidMapperMock.map(third)).thenReturn(validDto("IUD-3", "IUV-3"));

    ExportFileResult result = service.executeExport("extraction-id", request);

    List<String> entries = ZipUtils.readZipEntries(tempDir.resolve("extraction-id").resolve(result.files().getFirst()));
    assertEquals(2, entries.size());
    assertTrue(entries.get(0).matches("IPA1-DEBT_POSITIONS_PAID-\\d{14}-part001-1_0\\.csv"));
    assertTrue(entries.get(1).matches("IPA1-DEBT_POSITIONS_PAID-\\d{14}-part002-1_0\\.csv"));
    InOrder inOrder = inOrder(debtPositionPaidDaoMock);
    inOrder.verify(debtPositionPaidDaoMock).findByFilters("IPA1", List.of(), List.of(), null, null, 2, 0);
    inOrder.verify(debtPositionPaidDaoMock).findByFilters("IPA1", List.of(), List.of(), null, null, 2, 2);
  }

  @Test
  void whenCheckingSuperclassThenServiceUsesIpaSplitExportPattern() {
    assertEquals(SplitByIpaCodeBaseExportProcessingService.class, service.getClass().getSuperclass());
  }

  @Test
  void whenIpaCodesAreEmptyThenRejectExport() {
    ExtractionRequest request = new ExtractionRequest(List.of(), MigrationFileType.DEBT_POSITIONS_PAID);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> service.executeExport("extraction-id", request)
    );

    assertEquals("ipaCodes must not be empty", exception.getMessage());
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "BROKER_IPA",
      Map.of(MigrationFileType.DEBT_POSITIONS_PAID, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private DebtPositionPaid debtPositionPaid(String iud, String iuv) {
    DebtPositionPaid debtPositionPaid = new DebtPositionPaid();
    debtPositionPaid.setCodIud(iud);
    debtPositionPaid.setCodRpSilinviarpIdUnivocoVersamento(iuv);
    return debtPositionPaid;
  }

  private PuDebtPositionPaidDTO validDto(String iud, String iuv) {
    return PuDebtPositionPaidDTO.builder()
      .codIud(iud)
      .codIuv(iuv)
      .identificativoDominio("DOMAIN")
      .identificativoMessaggioRicevuta("RECEIPT")
      .dataOraMessaggioRicevuta(LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0))
      .codiceIdentificativoUnivocoAttestante("ATTESTANTE")
      .denominazioneAttestante("Attestante")
      .denominazioneBeneficiario("Beneficiario")
      .soggPagTipoIdentificativoUnivoco(it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType.F)
      .soggPagCodiceIdentificativoUnivoco("PAYER")
      .anagraficaPagatore("Payer")
      .codiceEsitoPagamento("0")
      .importoTotalePagato(BigDecimal.TEN)
      .identificativoUnivocoVersamento(iuv)
      .causaleVersamento("Causale")
      .indiceDatiSingoloPagamento(1)
      .codFiscalePa1("CFPA1")
      .build();
  }
}
