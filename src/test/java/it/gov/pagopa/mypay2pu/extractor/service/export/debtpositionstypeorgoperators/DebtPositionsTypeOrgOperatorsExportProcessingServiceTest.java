package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionstypeorgoperators;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionsTypeOrgOperatorsDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionsTypeOrgOperatorsDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionstypeorgoperators.DebtPositionsTypeOrgOperatorsMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
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
class DebtPositionsTypeOrgOperatorsExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private DebtPositionsTypeOrgOperatorsDao debtPositionsTypeOrgOperatorsDaoMock;

  @Mock
  private DebtPositionsTypeOrgOperatorsMapper debtPositionsTypeOrgOperatorsMapperMock;

  private DebtPositionsTypeOrgOperatorsExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new DebtPositionsTypeOrgOperatorsExportProcessingService(
      debtPositionsTypeOrgOperatorsDaoMock,
      debtPositionsTypeOrgOperatorsMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(debtPositionsTypeOrgOperatorsDaoMock, debtPositionsTypeOrgOperatorsMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedDebtPositionsTypeOrgOperatorsToArchive() throws Exception {
    ExtractionRequest request = new ExtractionRequest(List.of("ORG_IPA"), MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS, null, new ExtractionFilters());
    DebtPositionsTypeOrgOperators first = debtPositionsTypeOrgOperators("ORG_IPA", "CF1", "TYPE_1");
    DebtPositionsTypeOrgOperators second = invalidDebtPositionsTypeOrgOperators();
    PuDebtPositionsTypeOrgOperatorsDTO firstDto = dto("ORG_IPA", "CF1", "TYPE_1");
    PuDebtPositionsTypeOrgOperatorsDTO secondDto = invalidDto();

    when(debtPositionsTypeOrgOperatorsDaoMock.findByFilters("ORG_IPA", null, null, 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionsTypeOrgOperatorsDaoMock.findByFilters("ORG_IPA", null, null, 2, 2)).thenReturn(List.of());
    when(debtPositionsTypeOrgOperatorsMapperMock.map(first)).thenReturn(firstDto);
    when(debtPositionsTypeOrgOperatorsMapperMock.map(second)).thenReturn(secondDto);

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
    assertTrue(exportFileName.matches("BROKER_IPA-DEBT_POSITIONS_TYPE_ORG_OPERATORS-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("BROKER_IPA-DEBT_POSITIONS_TYPE_ORG_OPERATORS-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("BROKER_IPA").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("BROKER_IPA").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(1, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE_ORG_OPERATORS-\\d{14}-1_0\\.csv"));

    List<String> errorArchiveEntries = ZipUtils.readZipEntries(errorArchivePath);
    assertEquals(1, errorArchiveEntries.size());
    assertTrue(errorArchiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE_ORG_OPERATORS-\\d{14}-1_0\\.errors\\.csv"));

    InOrder inOrder = inOrder(debtPositionsTypeOrgOperatorsDaoMock);
    inOrder.verify(debtPositionsTypeOrgOperatorsDaoMock).findByFilters("ORG_IPA", null, null, 2, 0);
    inOrder.verify(debtPositionsTypeOrgOperatorsDaoMock).findByFilters("ORG_IPA", null, null, 2, 2);
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = new ExtractionRequest(List.of("ORG_IPA"), MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS, null, new ExtractionFilters());
    DebtPositionsTypeOrgOperators first = debtPositionsTypeOrgOperators("ORG_IPA", "CF1", "TYPE_1");
    DebtPositionsTypeOrgOperators second = debtPositionsTypeOrgOperators("ORG_IPA", "CF2", "TYPE_2");

    when(debtPositionsTypeOrgOperatorsDaoMock.findByFilters("ORG_IPA", null, null, 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionsTypeOrgOperatorsDaoMock.findByFilters("ORG_IPA", null, null, 2, 2)).thenReturn(List.of());
    when(debtPositionsTypeOrgOperatorsMapperMock.map(first)).thenReturn(dto("ORG_IPA", "CF1", "TYPE_1"));
    when(debtPositionsTypeOrgOperatorsMapperMock.map(second)).thenReturn(dto("ORG_IPA", "CF2", "TYPE_2"));

    ExportFileResult result = service.executeExport("BROKER_IPA", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("BROKER_IPA").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("ORG_IPA-DEBT_POSITIONS_TYPE_ORG_OPERATORS-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(debtPositionsTypeOrgOperatorsDaoMock);
    inOrder.verify(debtPositionsTypeOrgOperatorsDaoMock).findByFilters("ORG_IPA", null, null, 2, 0);
    inOrder.verify(debtPositionsTypeOrgOperatorsDaoMock).findByFilters("ORG_IPA", null, null, 2, 2);
  }

  @Test
  void whenCheckingSuperclassThenServiceIsSplitByIpaCodeBased() {
    assertEquals(SplitByIpaCodeBaseExportProcessingService.class, service.getClass().getSuperclass());
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "BROKER_IPA",
      Map.of(MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private DebtPositionsTypeOrgOperators debtPositionsTypeOrgOperators(String organizationIpaCode, String operatorFiscalCode, String debtPositionsTypeOrgCode) {
    return new DebtPositionsTypeOrgOperators(organizationIpaCode, operatorFiscalCode, debtPositionsTypeOrgCode);
  }

  private PuDebtPositionsTypeOrgOperatorsDTO dto(String organizationIpaCode, String operatorFiscalCode, String debtPositionsTypeOrgCode) {
    return PuDebtPositionsTypeOrgOperatorsDTO.builder()
      .organizationIpaCode(organizationIpaCode)
      .operatorFiscalCode(operatorFiscalCode)
      .debtPositionsTypeOrgCode(debtPositionsTypeOrgCode)
      .build();
  }

  private DebtPositionsTypeOrgOperators invalidDebtPositionsTypeOrgOperators() {
    return new DebtPositionsTypeOrgOperators("", "", "");
  }

  private PuDebtPositionsTypeOrgOperatorsDTO invalidDto() {
    return PuDebtPositionsTypeOrgOperatorsDTO.builder()
      .organizationIpaCode("")
      .operatorFiscalCode("")
      .debtPositionsTypeOrgCode("")
      .build();
  }
}
