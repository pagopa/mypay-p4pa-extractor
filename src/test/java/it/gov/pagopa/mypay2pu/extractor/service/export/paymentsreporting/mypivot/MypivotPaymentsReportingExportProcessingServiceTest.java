package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypivot;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentReportingMyPivotDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting.FlussoRiversamentoMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.service.xml.FlussoRiversamentoXmlMarshallerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MypivotPaymentsReportingExportProcessingServiceTest {

  @TempDir
  Path tempDir;
  @Mock
  private PaymentReportingMyPivotDao paymentReportingMyPivotDaoMock;
  @Mock
  private FlussoRiversamentoMapper flussoRiversamentoMapperMock;
  @Mock
  private FlussoRiversamentoXmlMarshallerService xmlMarshallerServiceMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(
      paymentReportingMyPivotDaoMock,
      flussoRiversamentoMapperMock,
      xmlMarshallerServiceMock
    );
  }

  @Test
  void givenFlowsWhenExportThenGenerateAndArchiveXmlFilesByPage() throws Exception {
    List<PaymentReporting> rows = List.of();
    FlussoRiversamento firstFlow = new FlussoRiversamento();
    FlussoRiversamento secondFlow = new FlussoRiversamento();
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 2, 0))
      .thenReturn(List.of("FLOW_1", "FLOW_2"));
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 2, 2))
      .thenReturn(List.of());
    when(paymentReportingMyPivotDaoMock.findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null))
      .thenReturn(rows);
    when(paymentReportingMyPivotDaoMock.findRowsByFlowIdentifier("IPA_CODE", "FLOW_2", null, null, null))
      .thenReturn(rows);
    when(flussoRiversamentoMapperMock.map(rows)).thenReturn(firstFlow, secondFlow);
    doAnswer(invocation -> {
      Files.writeString(invocation.getArgument(1), "<FlussoRiversamento/>");
      return null;
    }).when(xmlMarshallerServiceMock).marshal(firstFlow, tempDir.resolve("extraction-id").resolve("FLOW_1.xml"));
    doAnswer(invocation -> {
      Files.writeString(invocation.getArgument(1), "<FlussoRiversamento/>");
      return null;
    }).when(xmlMarshallerServiceMock).marshal(secondFlow, tempDir.resolve("extraction-id").resolve("FLOW_2.xml"));

    ExportFileResult result = service(2).executeExport("extraction-id", request());

    assertEquals(1, result.archiveFiles().size());
    ArchiveFile archiveFile = result.archiveFiles().getFirst();
    assertTrue(archiveFile.getName().matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part001-1\\.0\\.zip"));
    assertEquals(List.of("IPA_CODE-FLOW_1.xml", "IPA_CODE-FLOW_2.xml"), archiveFile.getFiles());
    Path zipPath = tempDir.resolve("extraction-id").resolve(archiveFile.getName());
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals("<FlussoRiversamento/>", new String(
        zipFile.getInputStream(zipFile.getEntry("IPA_CODE-FLOW_1.xml")).readAllBytes(), StandardCharsets.UTF_8
      ));
      assertEquals("<FlussoRiversamento/>", new String(
        zipFile.getInputStream(zipFile.getEntry("IPA_CODE-FLOW_2.xml")).readAllBytes(), StandardCharsets.UTF_8
      ));
    }
    assertFalse(Files.exists(tempDir.resolve("extraction-id").resolve("FLOW_1.xml")));
    assertFalse(Files.exists(tempDir.resolve("extraction-id").resolve("FLOW_2.xml")));
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 2, 0);
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 2, 2);
    verify(paymentReportingMyPivotDaoMock).findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null);
    verify(paymentReportingMyPivotDaoMock).findRowsByFlowIdentifier("IPA_CODE", "FLOW_2", null, null, null);
    verify(flussoRiversamentoMapperMock, times(2)).map(rows);
    verify(xmlMarshallerServiceMock).marshal(firstFlow, tempDir.resolve("extraction-id").resolve("FLOW_1.xml"));
    verify(xmlMarshallerServiceMock).marshal(secondFlow, tempDir.resolve("extraction-id").resolve("FLOW_2.xml"));
  }

  @Test
  void givenInvalidFlowWhenExportThenCreateErrorsArchiveWithFailureReason() throws Exception {
    List<PaymentReporting> rows = List.of();
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0))
      .thenReturn(List.of("FLOW_1"));
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1))
      .thenReturn(List.of());
    when(paymentReportingMyPivotDaoMock.findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null))
      .thenReturn(rows);
    when(flussoRiversamentoMapperMock.map(rows)).thenThrow(new IllegalArgumentException("Invalid flow data"));

    ExportFileResult result = service(1).executeExport("extraction-id", request());

    assertEquals(2, result.archiveFiles().size());
    ArchiveFile emptyArchive = result.archiveFiles().getFirst();
    assertTrue(emptyArchive.getName().matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part001-1\\.0\\.zip"));
    assertEquals(List.of(), emptyArchive.getFiles());
    Path emptyZipPath = tempDir.resolve("extraction-id").resolve(emptyArchive.getName());
    try (ZipFile emptyZip = new ZipFile(emptyZipPath.toFile())) {
      assertEquals(0, emptyZip.size());
    }
    ArchiveFile errorArchive = result.archiveFiles().get(1);
    assertTrue(errorArchive.getName().matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-1\\.0\\.errors\\.zip"));
    String errorFileName = errorArchive.getFiles().getFirst();
    assertTrue(errorFileName
      .matches("BROKER_IPA-IPA_CODE-PAYMENTS_REPORTING-\\d{14}-part001-1\\.0\\.errors\\.csv"));
    Path zipPath = tempDir.resolve("extraction-id").resolve(errorArchive.getName());
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      assertEquals(
        List.of(
          "\"flowIdentifier\";\"description\"",
          "\"FLOW_1\";\"Invalid flow data\""
        ),
        new String(zipFile.getInputStream(zipFile.getEntry(errorFileName)).readAllBytes(), StandardCharsets.UTF_8)
          .lines()
          .toList()
      );
    }
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0);
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1);
    verify(paymentReportingMyPivotDaoMock).findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null);
    verify(flussoRiversamentoMapperMock).map(rows);
  }

  @Test
  void givenTransientXmlWriteFailureWhenExportThenRetryAndArchiveFlow() {
    List<PaymentReporting> rows = List.of();
    FlussoRiversamento flow = new FlussoRiversamento();
    Path xmlPath = tempDir.resolve("extraction-id").resolve("FLOW_1.xml");
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0))
      .thenReturn(List.of("FLOW_1"));
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1))
      .thenReturn(List.of());
    when(paymentReportingMyPivotDaoMock.findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null))
      .thenReturn(rows);
    when(flussoRiversamentoMapperMock.map(rows)).thenReturn(flow);
    doThrow(new IllegalStateException("I/O failure", new IOException("Temporary write error")))
      .doAnswer(invocation -> {
        Files.writeString(invocation.getArgument(1), "<FlussoRiversamento/>");
        return null;
      })
      .when(xmlMarshallerServiceMock).marshal(flow, xmlPath);

    ExportFileResult result = service(1).executeExport("extraction-id", request());

    assertEquals(1, result.archiveFiles().size());
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0);
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1);
    verify(paymentReportingMyPivotDaoMock).findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null);
    verify(flussoRiversamentoMapperMock).map(rows);
    verify(xmlMarshallerServiceMock, times(2)).marshal(flow, xmlPath);
  }

  private MypivotPaymentsReportingExportProcessingService service(int pageSize) {
    return new MypivotPaymentsReportingExportProcessingService(
      paymentReportingMyPivotDaoMock,
      flussoRiversamentoMapperMock,
      xmlMarshallerServiceMock,
      new ExtractorExportProperties(
        tempDir.toString(),
        tempDir.toString(),
        "BROKER_CF",
        "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING_MYPIVOT, new ExtractorExportProperties.FileTypeConfiguration(pageSize))
      ),
      new CsvService(';', '"'),
      new ZipFileService()
    );
  }

  private ExtractionRequest request() {
    return new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING_MYPIVOT);
  }
}
