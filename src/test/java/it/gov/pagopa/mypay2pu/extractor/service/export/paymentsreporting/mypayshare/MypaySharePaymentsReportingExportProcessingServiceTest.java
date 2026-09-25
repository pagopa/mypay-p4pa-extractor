package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypayshare;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.config.PaymentsReportingSource;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentReportingMyPayShareDao;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MypaySharePaymentsReportingExportProcessingServiceTest {

  @TempDir
  Path tempDir;
  @Mock
  private PaymentReportingMyPayShareDao paymentReportingMyPayShareDaoMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(paymentReportingMyPayShareDaoMock);
  }

  @Test
  void givenDateRangeAndMissingXmlWhenExportThenSkipMissingFileAndCreateZipPreservingXmlName() throws Exception {
    Path xmlFile = tempDir.resolve("IPA_CODE").resolve("report.xml");
    Files.createDirectories(xmlFile.getParent());
    Files.writeString(xmlFile, "<report>content</report>");
    OffsetDateTime createdFrom = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime createdTo = OffsetDateTime.parse("2026-01-31T23:59:59Z");
    ExtractionRequest request = request(new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 0))
      .thenReturn(List.of(Path.of("report.xml")));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 1))
      .thenReturn(List.of(Path.of("missing.xml")));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 2))
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
        new String(zipFile.getInputStream(zipFile.getEntry("IPA_CODE-report.xml")).readAllBytes(), StandardCharsets.UTF_8)
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
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 0);
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 1);
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of(), 1, 2);
  }

  @Test
  void givenLogicalKeyAndMissingXmlWhenExportThenSkipMissingFileAndCreateEmptyZip() throws Exception {
    ExtractionRequest request = request(new ExtractionFilters().logicalKey("FLOW-1"));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of("FLOW-1"), 1, 0))
      .thenReturn(List.of(Path.of("missing.xml")));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of("FLOW-1"), 1, 1))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    assertTrue(Files.exists(zipPath));
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(0, zipFile.size());
    }
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of("FLOW-1"), 1, 0);
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of("FLOW-1"), 1, 1);
  }

  @Test
  void givenUnixStyleNestedFilePathWhenExportThenAddXmlFromNestedDirectoryToZip() throws Exception {
    Path xmlFile = tempDir.resolve("IPA_CODE").resolve("mypay").resolve("reporting").resolve("report.xml");
    Files.createDirectories(xmlFile.getParent());
    Files.writeString(xmlFile, "<report>content</report>");
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of(), 1, 0))
      .thenReturn(List.of(Path.of("/mypay/reporting/report.xml")));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of(), 1, 1))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request(null));

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(
        "<report>content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("IPA_CODE-report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of(), 1, 0);
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of(), 1, 1);
  }

  @Test
  void givenWindowsStyleNestedFilePathWhenExportThenAddXmlFromNestedDirectoryToZip() throws Exception {
    Path xmlFile = tempDir.resolve("IPA_CODE").resolve("mypay").resolve("reporting").resolve("report.xml");
    Files.createDirectories(xmlFile.getParent());
    Files.writeString(xmlFile, "<report>content</report>");
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of(), 1, 0))
      .thenReturn(List.of(Path.of("mypay\\reporting\\report.xml")));
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, null, null, List.of(), 1, 1))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request(null));

    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(
        "<report>content</report>",
        new String(zipFile.getInputStream(zipFile.getEntry("IPA_CODE-report.xml")).readAllBytes(), StandardCharsets.UTF_8)
      );
    }
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of(), 1, 0);
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, null, null, List.of(), 1, 1);
  }

  @Test
  void givenDateRangeAndLogicalKeyWhenExportThenApplyBothFilters() {
    OffsetDateTime createdFrom = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime createdTo = OffsetDateTime.parse("2026-01-31T23:59:59Z");
    ExtractionRequest request = request(
      new ExtractionFilters().dateFrom(createdFrom).dateTo(createdTo).logicalKey("FLOW-1")
    );
    when(paymentReportingMyPayShareDaoMock.findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of("FLOW-1"), 1, 0))
      .thenReturn(List.of());

    ExportFileResult result = service().executeExport("extraction-id", request);

    assertEquals(List.of(), result.files());
    verify(paymentReportingMyPayShareDaoMock).findByFilters("IPA_CODE", null, createdFrom, createdTo, List.of("FLOW-1"), 1, 0);
  }

  @Test
  void givenMypayShareSourceAndMissingSharedFolderWhenServiceCreatedThenThrowException() {
    Path missingDirectory = tempDir.resolve("missing");

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> service(missingDirectory, PaymentsReportingSource.MYPAY_SHARE)
    );

    assertEquals(
      "Property mypay.path.directory-root-enti must point to an existing directory: " + missingDirectory,
      exception.getMessage()
    );
  }

  @Test
  void givenMypivotSourceAndMissingSharedFolderWhenServiceCreatedThenDoNotThrowException() {
    assertDoesNotThrow(() -> service(tempDir.resolve("missing"), PaymentsReportingSource.MYPIVOT));
  }

  private MypaySharePaymentsReportingExportProcessingService service() {
    return service(tempDir, PaymentsReportingSource.MYPAY_SHARE);
  }

  private MypaySharePaymentsReportingExportProcessingService service(
    Path myPaySharedFolderPath,
    PaymentsReportingSource paymentsReportingSource
  ) {
    return new MypaySharePaymentsReportingExportProcessingService(
      paymentReportingMyPayShareDaoMock,
      new ExtractorExportProperties(
        tempDir.toString(), tempDir.toString(), "BROKER_CF", "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING, new ExtractorExportProperties.FileTypeConfiguration(1)),
        new ExtractorExportProperties.PaymentsReportingConfiguration(paymentsReportingSource)
      ),
      new MyPayProperties(
        new MyPayProperties.PathProperties(myPaySharedFolderPath.toString()),
        new MyPayProperties.GlobalProperties(null, null)
      ),
      new CsvService(';', '"'),
      new ZipFileService()
    );
  }

  private ExtractionRequest request(ExtractionFilters filters) {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING, null, filters);
  }
}
