package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
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
class PaymentsReportingExportProcessingServiceTest {

  @TempDir
  Path tempDir;
  @Mock
  private PaymentsReportingDao paymentsReportingDaoMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(paymentsReportingDaoMock);
  }

  @Test
  void givenMultiplePagesAndMissingXmlWhenExportThenCreateOneZipPerPagePreservingXmlNames() throws Exception {
    Path xmlFile = tempDir.resolve("xml").resolve("report.xml");
    Files.createDirectories(xmlFile.getParent());
    Files.writeString(xmlFile, "<report>content</report>");
    Path secondXmlFile = tempDir.resolve("xml").resolve("second-report.xml");
    Files.writeString(secondXmlFile, "<report>second-content</report>");
    OffsetDateTime createdFrom = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime createdTo = OffsetDateTime.parse("2026-01-31T23:59:59Z");
    ExtractionRequest request = request(new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo));
    when(paymentsReportingDaoMock.findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 0))
      .thenReturn(List.of("xml/report.xml", "missing.xml"));
    when(paymentsReportingDaoMock.findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 2))
      .thenReturn(List.of("xml/second-report.xml"));

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(2, result.files().size());
    assertTrue(result.files().get(0).matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part001-1\\.0\\.zip"));
    assertTrue(result.files().get(1).matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part002-1\\.0\\.zip"));
    try (ZipFile zipFile = new ZipFile(tempDir.resolve("extraction-id").resolve(result.files().get(0)).toFile())) {
      assertEquals(1, zipFile.size());
      assertEquals(
        "<report>content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    try (ZipFile zipFile = new ZipFile(tempDir.resolve("extraction-id").resolve(result.files().get(1)).toFile())) {
      assertEquals(1, zipFile.size());
      assertEquals(
        "<report>second-content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("second-report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    verify(paymentsReportingDaoMock).findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 0);
    verify(paymentsReportingDaoMock).findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 2);
  }

  @Test
  void givenLogicalKeyAndNoXmlWhenExportThenCreateEmptyZip() throws Exception {
    ExtractionRequest request = request(new ExtractionFilters().logicalKey("FLOW-1"));
    when(paymentsReportingDaoMock.findByLogicalKey("IPA_CODE", "FLOW-1", 2, 0))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(0, zipFile.size());
    }
    verify(paymentsReportingDaoMock).findByLogicalKey("IPA_CODE", "FLOW-1", 2, 0);
  }

  @Test
  void givenMultipleIpaCodesWhenExportThenCreateOneZipForEachIpaCode() throws Exception {
    Path firstXmlFile = tempDir.resolve("first.xml");
    Path secondXmlFile = tempDir.resolve("second.xml");
    Files.writeString(firstXmlFile, "<report>first</report>");
    Files.writeString(secondXmlFile, "<report>second</report>");
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_1", "IPA_2"), MigrationFileType.PAYMENTS_REPORTING, null, null
    );
    when(paymentsReportingDaoMock.findByDateRange("IPA_1", null, null, null, 2, 0))
      .thenReturn(List.of("first.xml"));
    when(paymentsReportingDaoMock.findByDateRange("IPA_2", null, null, null, 2, 0))
      .thenReturn(List.of("second.xml"));

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(2, result.files().size());
    assertTrue(result.files().stream().anyMatch(fileName -> fileName.startsWith("BROKER_IPA-IPA_1-PAYMENTS_REPORTING-")));
    assertTrue(result.files().stream().anyMatch(fileName -> fileName.startsWith("BROKER_IPA-IPA_2-PAYMENTS_REPORTING-")));
    result.files().forEach(fileName -> assertTrue(Files.exists(tempDir.resolve("extraction-id").resolve(fileName))));
    verify(paymentsReportingDaoMock).findByDateRange("IPA_1", null, null, null, 2, 0);
    verify(paymentsReportingDaoMock).findByDateRange("IPA_2", null, null, null, 2, 0);
  }

  private PaymentsReportingExportProcessingService service() {
    ExtractorExportProperties properties = new ExtractorExportProperties(
        tempDir.toString(), tempDir.toString(), "BROKER_CF", "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING, new ExtractorExportProperties.FileTypeConfiguration(2)),
        new ExtractorExportProperties.PaymentsReportingConfiguration(tempDir.toString())
    );
    return new PaymentsReportingExportProcessingService(
      paymentsReportingDaoMock,
      new PaymentsReportingPartitionWriterService(properties),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      properties
    );
  }

  private ExtractionRequest request(ExtractionFilters filters) {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING, null, filters);
  }
}
