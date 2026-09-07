package it.gov.pagopa.mypay2pu.extractor.service.export.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.TreasuryCsvCompleteDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuTreasuryCsvCompleteDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete.TreasuryCsvCompleteMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreasuryCsvCompleteExportProcessingServiceTest {

  @TempDir
  Path tempDir;
  @Mock
  private TreasuryCsvCompleteDao treasuryDaoMock;
  @Mock
  private TreasuryCsvCompleteMapper treasuryMapperMock;
  private TreasuryCsvCompleteExportProcessingService service;
  private ValidatorFactory validatorFactory;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    validatorFactory = Validation.buildDefaultValidatorFactory();
    service = new TreasuryCsvCompleteExportProcessingService(
      treasuryDaoMock, treasuryMapperMock, csvService, new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "password", new ZipFileService()),
      validatorFactory.getValidator(), properties()
    );
  }

  @AfterEach
  void verifyMocks() {
    validatorFactory.close();
    verifyNoMoreInteractions(treasuryDaoMock, treasuryMapperMock);
  }

  @Test
  void whenFullMultiOrganizationExportThenArchiveUsesStandardBrokerIpaName() throws Exception {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA1", "IPA2"), MigrationFileType.TREASURY_CSV_COMPLETE);
    TreasuryCsvComplete first = treasury("B1");
    TreasuryCsvComplete second = treasury("B2");
    when(treasuryDaoMock.findByFilters("IPA1", emptyFilters(), 2, 0)).thenReturn(List.of(first));
    when(treasuryDaoMock.findByFilters("IPA2", emptyFilters(), 2, 0)).thenReturn(List.of(second));
    when(treasuryMapperMock.map(first)).thenReturn(dto("B1"));
    when(treasuryMapperMock.map(second)).thenReturn(dto("B2"));

    ExportFileResult result = service.executeExport("extraction", request);

    assertTrue(result.files().getFirst().matches("BROKER_IPA-TREASURY_CSV_COMPLETE-\\d{14}-1_0\\.zip"));
    try (ZipFile zipFile = new ZipFile(tempDir.resolve("extraction").resolve(result.files().getFirst()).toFile())) {
      assertEquals(2, zipFile.size());
      List<String> entryNames = zipFile.stream().map(java.util.zip.ZipEntry::getName).toList();
      assertTrue(entryNames.get(0).matches("IPA1-TREASURY_CSV_COMPLETE-\\d{14}-1_0\\.csv"));
      assertTrue(entryNames.get(1).matches("IPA2-TREASURY_CSV_COMPLETE-\\d{14}-1_0\\.csv"));
    }
    verify(treasuryDaoMock).findByFilters("IPA1", emptyFilters(), 2, 0);
    verify(treasuryDaoMock).findByFilters("IPA2", emptyFilters(), 2, 0);
    verify(treasuryMapperMock).map(first);
    verify(treasuryMapperMock).map(second);
  }

  @Test
  void whenIncrementalOrLogicalKeyExportThenDelegateStandardDateFilters() {
    OffsetDateTime updatedFrom = OffsetDateTime.parse("2026-01-02T00:00:00Z");
    ExtractionFilters filters = new ExtractionFilters()
      .logicalKey("2026|B1").dateFrom(updatedFrom);
    ExtractionRequest request = new ExtractionRequest(List.of("IPA1"), MigrationFileType.TREASURY_CSV_COMPLETE, null, filters);
    TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters expected =
      new TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters("2026|B1", updatedFrom, null);
    when(treasuryDaoMock.findByFilters("IPA1", expected, 50, 0)).thenReturn(List.of());

    assertEquals(List.of(), service.retrieveData("IPA1", request, 50, 0));

    verify(treasuryDaoMock).findByFilters("IPA1", expected, 50, 0);
  }

  @Test
  void whenRowsExceedConfiguredLimitThenArchiveContainsNumberedParts() throws Exception {
    ExtractionRequest request = new ExtractionRequest(List.of("IPA1"), MigrationFileType.TREASURY_CSV_COMPLETE);
    TreasuryCsvComplete first = treasury("B1");
    TreasuryCsvComplete second = treasury("B2");
    TreasuryCsvComplete third = treasury("B3");
    when(treasuryDaoMock.findByFilters("IPA1", emptyFilters(), 2, 0)).thenReturn(List.of(first, second));
    when(treasuryDaoMock.findByFilters("IPA1", emptyFilters(), 2, 2)).thenReturn(List.of(third));
    when(treasuryMapperMock.map(first)).thenReturn(dto("B1"));
    when(treasuryMapperMock.map(second)).thenReturn(dto("B2"));
    when(treasuryMapperMock.map(third)).thenReturn(dto("B3"));

    ExportFileResult result = service.executeExport("extraction", request);

    try (ZipFile zipFile = new ZipFile(tempDir.resolve("extraction").resolve(result.files().getFirst()).toFile())) {
      List<String> names = zipFile.stream().map(java.util.zip.ZipEntry::getName).toList();
      assertEquals(2, names.size());
      assertTrue(names.get(0).matches("IPA1-TREASURY_CSV_COMPLETE-\\d{14}-part001-1_0\\.csv"));
      assertTrue(names.get(1).matches("IPA1-TREASURY_CSV_COMPLETE-\\d{14}-part002-1_0\\.csv"));
    }
    verify(treasuryDaoMock).findByFilters("IPA1", emptyFilters(), 2, 0);
    verify(treasuryDaoMock).findByFilters("IPA1", emptyFilters(), 2, 2);
    verify(treasuryMapperMock).map(first);
    verify(treasuryMapperMock).map(second);
    verify(treasuryMapperMock).map(third);
  }

  private ExtractorExportProperties properties() {
    return new ExtractorExportProperties(tempDir.toString(), tempDir.toString(), "12345678901", "BROKER_IPA",
      Map.of(MigrationFileType.TREASURY_CSV_COMPLETE, new ExtractorExportProperties.FileTypeConfiguration(2)));
  }

  private TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters emptyFilters() {
    return new TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters(null, null, null);
  }

  private TreasuryCsvComplete treasury(String code) {
    return new TreasuryCsvComplete("2026", code, null, null, "IPA1", "IUF", "IUV", "ACCOUNT", "DOMAIN", "TYPE",
      "CAUSE", "Cause", BigDecimal.ONE, LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0),
      LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0), "2026", "DOC", "SEAL", "Last", "First", "Street",
      "00100", "Rome", "CF", "VAT", "ABI", "CAB", "IBAN", "REGISTRY", "AE", "PROVISIONAL", "ACCOUNT_TYPE",
      "PROCESS", "EXECUTION", "TRANSFER", 1L, LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0), true,
      LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0), "MANAGEMENT", "END");
  }

  private PuTreasuryCsvCompleteDTO dto(String code) {
    return PuTreasuryCsvCompleteDTO.builder().billYear("2026").billCode(code).organizationIpaCode("IPA1").build();
  }
}
