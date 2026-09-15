package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.config.MyPayPathProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
class MypaySharePaymentsReportingExportProcessingServiceTest {

  @TempDir
  Path tempDir;
  @Mock
  private PaymentsReportingDao paymentsReportingDaoMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(paymentsReportingDaoMock);
  }

  @Test
  void givenDateRangeAndMissingXmlWhenExportThenSkipMissingFileAndCreateZipPreservingXmlName() throws Exception {
    Path xmlFile = tempDir.resolve("IPA_CODE").resolve("report.xml");
    Files.createDirectories(xmlFile.getParent());
    Files.writeString(xmlFile, "<report>content</report>");
    OffsetDateTime createdFrom = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime createdTo = OffsetDateTime.parse("2026-01-31T23:59:59Z");
    ExtractionRequest request = request(new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo));
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 0))
      .thenReturn(List.of(Path.of("report.xml")));
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 1))
      .thenReturn(List.of(Path.of("missing.xml")));
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 2))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(3, result.files().size());
    assertTrue(result.files().getFirst().matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part001-1\\.0\\.zip"));
    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(1, zipFile.size());
      assertEquals(
        "<report>content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    assertTrue(Files.exists(xmlFile));
    Path discardFilePath = tempDir.resolve("extraction-id").resolve(result.files().get(2));
    assertTrue(discardFilePath.getFileName().toString()
      .matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part002-1\\.0\\.errors\\.csv"));
    assertEquals(
      List.of(
        "\"fileName\";\"description\"",
        "\"missing.xml\";\"XML file not found on the MyPay share\""
      ),
      Files.readAllLines(discardFilePath)
    );
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 0);
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 1);
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, null, 1, 2);
  }

  @Test
  void givenLogicalKeyAndMissingXmlWhenExportThenSkipMissingFileAndCreateEmptyZip() throws Exception {
    ExtractionRequest request = request(new ExtractionFilters().logicalKey("FLOW-1"));
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, null, null, "FLOW-1", 1, 0))
      .thenReturn(List.of(Path.of("missing.xml")));
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, null, null, "FLOW-1", 1, 1))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(0, zipFile.size());
    }
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, null, null, "FLOW-1", 1, 0);
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, null, null, "FLOW-1", 1, 1);
  }

  @Test
  void givenDateRangeAndLogicalKeyWhenExportThenApplyBothFilters() {
    OffsetDateTime createdFrom = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime createdTo = OffsetDateTime.parse("2026-01-31T23:59:59Z");
    ExtractionRequest request = request(
      new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo).logicalKey("FLOW-1")
    );
    when(paymentsReportingDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, "FLOW-1", 1, 0))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(List.of(), result.files());
    verify(paymentsReportingDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, "FLOW-1", 1, 0);
  }

  private MypaySharePaymentsReportingExportProcessingService service() {
    return new MypaySharePaymentsReportingExportProcessingService(
      paymentsReportingDaoMock,
      new ExtractorExportProperties(
        tempDir.toString(), tempDir.toString(), "BROKER_CF", "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING, new ExtractorExportProperties.FileTypeConfiguration(1)),
        ExtractorExportProperties.PaymentsReportingConfiguration.mypayShare()
      ),
      new MyPayPathProperties(tempDir.toString()),
      new CsvService(';', '"'),
      new ZipFileService()
    );
  }

  private ExtractionRequest request(ExtractionFilters filters) {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING, null, filters);
  }
}
