package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
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
  void givenDateRangeAndMissingXmlWhenExportThenSkipMissingFileAndCreateZipPreservingXmlName() throws Exception {
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

    assertEquals(1, result.files().size());
    assertTrue(result.files().getFirst().matches("IPA_CODE_1\\.0_\\d{14}\\.zip"));
    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(2, zipFile.size());
      assertEquals(
        "<report>content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
      assertEquals(
        "<report>second-content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("second-report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    verify(paymentsReportingDaoMock).findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 0);
    verify(paymentsReportingDaoMock).findByDateRange("IPA_CODE", null, createdFrom, createdTo, 2, 2);
  }

  @Test
  void givenLogicalKeyAndMissingXmlWhenExportThenSkipMissingFileAndCreateEmptyZip() throws Exception {
    ExtractionRequest request = request(new ExtractionFilters().logicalKey("FLOW-1"));
    when(paymentsReportingDaoMock.findByLogicalKey("IPA_CODE", "FLOW-1", 2, 0))
      .thenReturn(List.of("missing.xml"));

    ExportFileResult result = service().executeExport("extraction-id", request);

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(0, zipFile.size());
    }
    verify(paymentsReportingDaoMock).findByLogicalKey("IPA_CODE", "FLOW-1", 2, 0);
  }

  private PaymentsReportingExportProcessingService service() {
    return new PaymentsReportingExportProcessingService(
      paymentsReportingDaoMock,
      new ExtractorExportProperties(
        tempDir.toString(), tempDir.toString(), "BROKER_CF", "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING, new ExtractorExportProperties.FileTypeConfiguration(2)),
        new ExtractorExportProperties.PaymentsReportingConfiguration(tempDir.toString())
      ),
      new ZipFileService()
    );
  }

  private ExtractionRequest request(ExtractionFilters filters) {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING, null, filters);
  }
}
