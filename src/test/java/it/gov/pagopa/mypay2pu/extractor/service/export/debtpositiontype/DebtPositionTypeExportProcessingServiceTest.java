package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontype;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontype.DebtPositionTypeMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.ZipUtils;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private DebtPositionTypeDao debtPositionTypeDaoMock;

  @Mock
  private DebtPositionTypeMapper debtPositionTypeMapperMock;

  private DebtPositionTypeExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new DebtPositionTypeExportProcessingService(
      debtPositionTypeDaoMock,
      debtPositionTypeMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(debtPositionTypeDaoMock, debtPositionTypeMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedDebtPositionTypesToArchive() throws Exception {
    ExtractionRequest request = new ExtractionRequest("ORG_IPA", MigrationFileType.DEBT_POSITIONS_TYPE)
      .filters(new ExtractionFilters());
    DebtPositionType first = debtPositionType("TYPE_1");
    DebtPositionType second = invalidDebtPositionType();
    PuDebtPositionTypeDTO firstDto = dto("TYPE_1");
    PuDebtPositionTypeDTO secondDto = invalidDto();
    when(debtPositionTypeDaoMock.findByFilters(request.getFilters(), 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionTypeDaoMock.findByFilters(request.getFilters(), 2, 2)).thenReturn(List.of());
    when(debtPositionTypeMapperMock.map(first)).thenReturn(firstDto);
    when(debtPositionTypeMapperMock.map(second)).thenReturn(secondDto);

    ExportFileResult result = service.executeExport("BROKER_IPA", request);

    assertNull(result.error());
    assertEquals(2, result.files().size());
    String exportFileName = result.files().stream()
      .filter(fileName -> !fileName.contains(".errors."))
      .findFirst()
      .orElseThrow();
    String errorFileName = result.files().stream()
      .filter(fileName -> fileName.contains(".errors."))
      .findFirst()
      .orElseThrow();
    assertTrue(exportFileName.matches("BROKER_IPA-DEBT_POSITIONS_TYPE-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("BROKER_IPA-DEBT_POSITIONS_TYPE-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("BROKER_IPA").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("BROKER_IPA").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(1, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE-\\d{14}-1_0\\.csv"));

    List<String> errorArchiveEntries = ZipUtils.readZipEntries(errorArchivePath);
    assertEquals(1, errorArchiveEntries.size());
    assertTrue(errorArchiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE-\\d{14}-1_0\\.errors\\.csv"));

    InOrder inOrder = inOrder(debtPositionTypeDaoMock);
    inOrder.verify(debtPositionTypeDaoMock).findByFilters(request.getFilters(), 2, 0);
    inOrder.verify(debtPositionTypeDaoMock).findByFilters(request.getFilters(), 2, 2);

  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = new ExtractionRequest("ORG_IPA", MigrationFileType.DEBT_POSITIONS_TYPE)
      .filters(new ExtractionFilters());
    DebtPositionType first = debtPositionType("TYPE_1");
    DebtPositionType second = debtPositionType("TYPE_2");
    when(debtPositionTypeDaoMock.findByFilters(request.getFilters(), 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionTypeDaoMock.findByFilters(request.getFilters(), 2, 2)).thenReturn(List.of());
    when(debtPositionTypeMapperMock.map(first)).thenReturn(dto("TYPE_1"));
    when(debtPositionTypeMapperMock.map(second)).thenReturn(dto("TYPE_2"));

    ExportFileResult result = service.executeExport("BROKER_IPA", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("BROKER_IPA").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(debtPositionTypeDaoMock);
    inOrder.verify(debtPositionTypeDaoMock).findByFilters(request.getFilters(), 2, 0);
    inOrder.verify(debtPositionTypeDaoMock).findByFilters(request.getFilters(), 2, 2);

  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "BROKER_IPA",
      Map.of(MigrationFileType.DEBT_POSITIONS_TYPE, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private DebtPositionType debtPositionType(String code) {
    return new DebtPositionType(
      "12345678901",
      code,
      "Description " + code,
      "ORG_TYPE",
      "MACRO_AREA",
      "SERVICE_TYPE",
      "COLLECTING_REASON",
      "9/12345678901234567",
      false,
      false,
      false
    );
  }

  private PuDebtPositionTypeDTO dto(String code) {
    return PuDebtPositionTypeDTO.builder()
      .brokerCf("12345678901")
      .debtPositionTypeCode(code)
      .description("Description " + code)
      .orgType("ORG_TYPE")
      .macroArea("MACRO_AREA")
      .serviceType("SERVICE_TYPE")
      .collectingReason("COLLECTING_REASON")
      .taxonomyCode("9/12345678901234567")
      .flagAnonymousFiscalCode(false)
      .flagMandatoryDueDate(false)
      .flagNotifyIo(false)
      .ioTemplateMessage("IO message")
      .ioTemplateSubject("IO subject")
      .build();
  }

  private DebtPositionType invalidDebtPositionType() {
    return new DebtPositionType(
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      null,
      null,
      null
    );
  }

  private PuDebtPositionTypeDTO invalidDto() {
    return PuDebtPositionTypeDTO.builder()
      .brokerCf("")
      .debtPositionTypeCode("")
      .description("")
      .orgType("")
      .macroArea("")
      .serviceType("")
      .collectingReason("")
      .taxonomyCode("")
      .flagAnonymousFiscalCode(null)
      .flagMandatoryDueDate(null)
      .flagNotifyIo(null)
      .build();
  }
}
