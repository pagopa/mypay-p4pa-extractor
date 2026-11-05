package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting.mypivot;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentReportingMyPivotDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting.FlussoRiversamentoMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.XMLMarshallerService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.service.xml.FlussoRiversamentoXmlMarshallerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MypivotPaymentsReportingExportProcessingServiceIntegrationTest {

  @TempDir
  Path tempDir;
  @Mock
  private PaymentReportingMyPivotDao paymentReportingMyPivotDaoMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(paymentReportingMyPivotDaoMock);
  }

  @Test
  void givenFlowRowsWhenExportThenArchiveXsdValidatedXml() throws Exception {
    List<PaymentReporting> rows = List.of(paymentReporting());
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0))
      .thenReturn(List.of("FLOW_1"));
    when(paymentReportingMyPivotDaoMock.findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1))
      .thenReturn(List.of());
    when(paymentReportingMyPivotDaoMock.findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null))
      .thenReturn(rows);

    ExportFileResult result = service().executeExport(
      "extraction-id",
      new ExtractionRequest(List.of("IPA_CODE"), MigrationFileType.PAYMENTS_REPORTING_MYPIVOT)
    );

    assertEquals(1, result.archiveFiles().size());
    Path zipPath = tempDir.resolve("extraction-id").resolve(result.files().getFirst());
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      String xml = new String(
        zipFile.getInputStream(zipFile.getEntry("IPA_CODE-FLOW_1.xml")).readAllBytes(),
        StandardCharsets.UTF_8
      );
      assertTrue(xml.contains("<FlussoRiversamento xmlns=\"http://www.digitpa.gov.it/schemas/2011/Pagamenti/\""));
      assertTrue(xml.contains("<identificativoFlusso>FLOW_1</identificativoFlusso>"));
      assertTrue(xml.contains("<importoTotalePagamenti>23.00</importoTotalePagamenti>"));
    }
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 0);
    verify(paymentReportingMyPivotDaoMock).findFlowIdentifiers("IPA_CODE", null, null, null, List.of(), 1, 1);
    verify(paymentReportingMyPivotDaoMock).findRowsByFlowIdentifier("IPA_CODE", "FLOW_1", null, null, null);
  }

  private MypivotPaymentsReportingExportProcessingService service() {
    return new MypivotPaymentsReportingExportProcessingService(
      paymentReportingMyPivotDaoMock,
      new FlussoRiversamentoMapper(),
      new FlussoRiversamentoXmlMarshallerService(new XMLMarshallerService()),
      new ExtractorExportProperties(
        tempDir.toString(),
        tempDir.toString(),
        "BROKER_CF",
        "BROKER_IPA",
        Map.of(MigrationFileType.PAYMENTS_REPORTING_MYPIVOT, new ExtractorExportProperties.FileTypeConfiguration(1))
      ),
      new CsvService(';', '"'),
      new ZipFileService()
    );
  }

  private PaymentReporting paymentReporting() {
    return new PaymentReporting(
      "FLOW_1",
      "1.1",
      LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30),
      "REG_1",
      LocalDate.of(2026, Month.JANUARY, 15),
      "B",
      "PPAYITR1XXX",
      "Poste Pay",
      "PPAYITR1XXX",
      "G",
      "80050050154",
      "Organization",
      "IUV_1",
      "IUR_1",
      1,
      new BigDecimal("23.00"),
      "0",
      LocalDate.of(2026, Month.JANUARY, 15)
    );
  }
}
