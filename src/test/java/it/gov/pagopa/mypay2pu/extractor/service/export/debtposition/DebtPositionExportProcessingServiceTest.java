package it.gov.pagopa.mypay2pu.extractor.service.export.debtposition;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtposition.DebtPositionMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.ZipUtils;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import jakarta.validation.Validation;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private DebtPositionDao debtPositionDaoMock;

  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new DebtPositionExportProcessingService(
      debtPositionDaoMock,
      debtPositionMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(debtPositionDaoMock, debtPositionMapperMock);
  }

  @Test
  void whenMultipleIpaCodesThenExportDebtPositionsByIpaCode() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      List.of("ORG_IPA", "ORG_IPA_2"),
      MigrationFileType.DEBT_POSITIONS,
      null,
      new ExtractionFilters()
    );
    DebtPosition first = debtPosition("IUPD-1", "IUD-1");
    DebtPosition second = debtPosition("IUPD-2", "IUD-2");

    when(debtPositionDaoMock.findDebtPositions("ORG_IPA", null, null, request.getFilters(), 2, 0)).thenReturn(List.of(first));
    when(debtPositionDaoMock.findDebtPositions("ORG_IPA_2", null, null, request.getFilters(), 2, 0)).thenReturn(List.of(second));
    when(debtPositionMapperMock.map(first, Action.I)).thenReturn(validDto("IUD-1"));
    when(debtPositionMapperMock.map(second, Action.I)).thenReturn(validDto("IUD-2"));

    ExportFileResult result = service.executeExport("BROKER_IPA", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    String exportFileName = result.files().getFirst();
    assertTrue(exportFileName.matches("BROKER_IPA-DEBT_POSITIONS-\\d{14}-2_0\\.zip"));

    Path exportArchivePath = tempDir.resolve("BROKER_IPA").resolve(exportFileName);
    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(2, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS-\\d{14}-2_0\\.csv"));
    assertTrue(exportArchiveEntries.get(1).matches("ORG_IPA_2-DEBT_POSITIONS-\\d{14}-2_0\\.csv"));

    InOrder inOrder = inOrder(debtPositionDaoMock);
    inOrder.verify(debtPositionDaoMock).findDebtPositions("ORG_IPA", null, null, request.getFilters(), 2, 0);
    inOrder.verify(debtPositionDaoMock).findDebtPositions("ORG_IPA_2", null, null, request.getFilters(), 2, 0);
  }

  @Test
  void whenCheckingSuperclassThenServiceIsSplitByIpa() {
    assertEquals(SplitByIpaCodeBaseExportProcessingService.class, service.getClass().getSuperclass());
  }

  @Test
  void whenIncrementalExtractionThenAssignsActionsToOpenAndCancelledDebtPositions() {
    ExtractionFilters filters = new ExtractionFilters();
    ExtractionRequest request = new ExtractionRequest(
      List.of("ORG_IPA"),
      MigrationFileType.DEBT_POSITIONS,
      OffsetDateTime.parse("2026-01-15T10:00:00Z"),
      filters
    );
    DebtPosition inserted = debtPosition("IUPD-INSERTED", "IUD-INSERTED", LocalDate.of(2026, Month.JANUARY, 16).atStartOfDay(), null);
    DebtPosition modified = debtPosition("IUPD-MODIFIED", "IUD-MODIFIED", LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(), LocalDate.of(2026, Month.JANUARY, 16).atStartOfDay());
    DebtPosition unchanged = debtPosition("IUPD-UNCHANGED", "IUD-UNCHANGED", LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(), LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30));
    DebtPosition withoutLastModificationDate = debtPosition("IUPD-NO-MODIFICATION-DATE", "IUD-NO-MODIFICATION-DATE", LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(), null);
    DebtPosition withoutCreationDate = debtPosition("IUPD-NO-CREATION-DATE", "IUD-NO-CREATION-DATE", null, LocalDate.of(2026, Month.JANUARY, 16).atStartOfDay());
    DebtPosition withoutChangeDates = debtPosition("IUPD-NO-CHANGE-DATES", "IUD-NO-CHANGE-DATES", null, null);
    DebtPosition cancelled = debtPosition("IUPD-CANCELLED", "IUD-CANCELLED");

    when(debtPositionDaoMock.findDebtPositions("ORG_IPA", null, null, filters, 10, 0))
      .thenReturn(List.of(inserted, modified, unchanged, withoutLastModificationDate, withoutCreationDate, withoutChangeDates));
    when(debtPositionDaoMock.findCancelledDebtPositions("ORG_IPA", null, null, filters, 10, 0))
      .thenReturn(List.of(cancelled));

    List<DebtPositionExportProcessingService.DebtPositionWithAction> result =
      service.retrieveData("ORG_IPA", request, 10, 0);

    assertEquals(
      List.of(Action.M, Action.M, Action.M, Action.I, Action.M, Action.I, Action.A),
      result.stream().map(DebtPositionExportProcessingService.DebtPositionWithAction::action).toList()
    );
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "BROKER_IPA",
      Map.of(MigrationFileType.DEBT_POSITIONS, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private PuDebtPositionDTO validDto(String iud) {
    return PuDebtPositionDTO.builder()
      .iupdOrg("IUPD_" + iud)
      .paymentOptionIndex(1)
      .paymentOptionType("SINGLE_INSTALLMENT")
      .iud(iud)
      .entityType(PersonEntityType.F)
      .fiscalCode("CF123")
      .fullName("John Doe")
      .amount(BigDecimal.TEN)
      .debtPositionTypeCode("TYPE")
      .remittanceInformation("Remittance")
      .generateNotice(true)
      .flagPuPagoPaPayment(true)
      .action(Action.I)
      .build();
  }

  private DebtPosition debtPosition(String iupd, String iud) {
    return debtPosition(
      iupd,
      iud,
      LocalDate.of(2026, Month.JANUARY, 10).atStartOfDay(),
      LocalDate.of(2026, Month.JANUARY, 11).atStartOfDay()
    );
  }

  private DebtPosition debtPosition(String iupd, String iud, LocalDateTime dtCreazione, LocalDateTime dtUltimaModifica) {
    return new DebtPosition(
      iupd,
      "description",
      LocalDate.of(2026, Month.JANUARY, 15),
      false,
      LocalDate.of(2026, Month.JANUARY, 16),
      1,
      "SINGLE_INSTALLMENT",
      "Pagamento Singolo Avviso",
      iud,
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
      dtCreazione,
      dtUltimaModifica
    );
  }
}
