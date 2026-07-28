package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontypeorg;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeOrgDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeOrgDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontypeorg.DebtPositionTypeOrgMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
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
class DebtPositionTypeOrgExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private DebtPositionTypeOrgDao debtPositionTypeOrgDaoMock;

  @Mock
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapperMock;

  private DebtPositionTypeOrgExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new DebtPositionTypeOrgExportProcessingService(
      debtPositionTypeOrgDaoMock,
      debtPositionTypeOrgMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(debtPositionTypeOrgDaoMock, debtPositionTypeOrgMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedDebtPositionTypeOrgsToArchive() throws Exception {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.DEBT_POSITIONS_TYPE_ORG, new ExtractionFilters());
    DebtPositionTypeOrg first = debtPositionTypeOrg("first");
    DebtPositionTypeOrg second = invalidDebtPositionTypeOrg();
    PuDebtPositionTypeOrgDTO firstDto = dto("first");
    PuDebtPositionTypeOrgDTO secondDto = invalidDto();
    when(debtPositionTypeOrgDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionTypeOrgDaoMock.findByFilters("IPA_CODE", null, 2, 2)).thenReturn(List.of());
    when(debtPositionTypeOrgMapperMock.map(first)).thenReturn(firstDto);
    when(debtPositionTypeOrgMapperMock.map(second)).thenReturn(secondDto);

    ExportFileResult result = service.executeExport("IPA_CODE", request);

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
    assertTrue(exportFileName.matches("IPA_CODE-DEBT_POSITIONS_TYPE_ORG-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("IPA_CODE-DEBT_POSITIONS_TYPE_ORG-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("IPA_CODE").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(1, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("IPA_CODE-DEBT_POSITIONS_TYPE_ORG-\\d{14}-1_0\\.csv"));

    List<String> errorArchiveEntries = ZipUtils.readZipEntries(errorArchivePath);
    assertEquals(1, errorArchiveEntries.size());
    assertTrue(errorArchiveEntries.get(0).matches("IPA_CODE-DEBT_POSITIONS_TYPE_ORG-\\d{14}-1_0\\.errors\\.csv"));

    InOrder inOrder = inOrder(debtPositionTypeOrgDaoMock);
    inOrder.verify(debtPositionTypeOrgDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(debtPositionTypeOrgDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.DEBT_POSITIONS_TYPE_ORG, new ExtractionFilters());
    DebtPositionTypeOrg first = debtPositionTypeOrg("first");
    DebtPositionTypeOrg second = debtPositionTypeOrg("second");
    when(debtPositionTypeOrgDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first, second));
    when(debtPositionTypeOrgDaoMock.findByFilters("IPA_CODE", null, 2, 2)).thenReturn(List.of());
    when(debtPositionTypeOrgMapperMock.map(first)).thenReturn(dto("first"));
    when(debtPositionTypeOrgMapperMock.map(second)).thenReturn(dto("second"));

    ExportFileResult result = service.executeExport("IPA_CODE", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("IPA_CODE").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("IPA_CODE-DEBT_POSITIONS_TYPE_ORG-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(debtPositionTypeOrgDaoMock);
    inOrder.verify(debtPositionTypeOrgDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(debtPositionTypeOrgDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "IPA_CODE",
      Map.of(MigrationFileType.DEBT_POSITIONS_TYPE_ORG, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private DebtPositionTypeOrg debtPositionTypeOrg(String suffix) {
    return new DebtPositionTypeOrg(
      "IPA_CODE",
      "BALANCE",
      "CODE_" + suffix,
      "Description " + suffix,
      "IT60X0542811101000000123456",
      null,
      null,
      null,
      "ORG_SECTOR",
      100L,
      "http://example.com",
      false,
      false,
      true,
      false,
      false,
      true,
      false,
      null,
      false,
      null,
      "Application " + suffix,
      "FORM_" + suffix
    );
  }

  private PuDebtPositionTypeOrgDTO dto(String suffix) {
    return PuDebtPositionTypeOrgDTO.builder()
      .ipaCode("IPA_CODE")
      .balance("BALANCE")
      .code("CODE_" + suffix)
      .description("Description " + suffix)
      .iban("IT60X0542811101000000123456")
      .flagAnonymousFiscalCode("false")
      .flagMandatoryDueDate("false")
      .flagSpontaneous("true")
      .flagNotifyIo("false")
      .flagActive("true")
      .flagNotifyOutcomePush("false")
      .flagAmountActualization("false")
      .flagExternal("false")
      .build();
  }

  private DebtPositionTypeOrg invalidDebtPositionTypeOrg() {
    return new DebtPositionTypeOrg(
      "IPA_CODE",
      "",
      "",
      "",
      "",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }

  private PuDebtPositionTypeOrgDTO invalidDto() {
    return PuDebtPositionTypeOrgDTO.builder()
      .ipaCode("")
      .code("")
      .description("")
      .iban("")
      .flagAnonymousFiscalCode("INVALID")
      .flagMandatoryDueDate("INVALID")
      .flagSpontaneous("INVALID")
      .flagNotifyIo("INVALID")
      .flagActive("INVALID")
      .flagNotifyOutcomePush("INVALID")
      .flagAmountActualization("INVALID")
      .flagExternal("INVALID")
      .build();
  }
}
