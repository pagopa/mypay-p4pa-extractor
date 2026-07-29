package it.gov.pagopa.mypay2pu.extractor.service.export.orgsil;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.OrgSilServiceDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrgSilServiceDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.orgsil.OrgSilServiceMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
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
class OrgSilServiceExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private OrgSilServiceDao orgSilServiceDaoMock;

  @Mock
  private OrgSilServiceMapper orgSilServiceMapperMock;

  private OrgSilServiceExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new OrgSilServiceExportProcessingService(
      orgSilServiceDaoMock,
      orgSilServiceMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(orgSilServiceDaoMock, orgSilServiceMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedOrgSilServicesToArchive() throws Exception {
    ExtractionRequest request = new ExtractionRequest("ORG_IPA", MigrationFileType.ORG_SIL_SERVICES);
    OrgSilService first = orgSilService("first");
    OrgSilService second = invalidOrgSilService();
    PuOrgSilServiceDTO firstDto = dto("first");
    PuOrgSilServiceDTO secondDto = invalidDto();
    when(orgSilServiceDaoMock.findPaidNotificationOutcome("ORG_IPA", 2, 0)).thenReturn(List.of(first));
    when(orgSilServiceDaoMock.findActualization("ORG_IPA", 2, 0)).thenReturn(List.of(second));
    when(orgSilServiceDaoMock.findPaidNotificationOutcome("ORG_IPA", 2, 2)).thenReturn(List.of());
    when(orgSilServiceDaoMock.findActualization("ORG_IPA", 2, 2)).thenReturn(List.of());
    when(orgSilServiceMapperMock.map(first)).thenReturn(firstDto);
    when(orgSilServiceMapperMock.map(second)).thenReturn(secondDto);

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
    assertTrue(exportFileName.matches("BROKER_IPA-ORG_SIL_SERVICES-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("BROKER_IPA-ORG_SIL_SERVICES-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("BROKER_IPA").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("BROKER_IPA").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(1, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("ORG_IPA-ORG_SIL_SERVICES-\\d{14}-1_0\\.csv"));

    List<String> errorArchiveEntries = ZipUtils.readZipEntries(errorArchivePath);
    assertEquals(1, errorArchiveEntries.size());
    assertTrue(errorArchiveEntries.get(0).matches("ORG_IPA-ORG_SIL_SERVICES-\\d{14}-1_0\\.errors\\.csv"));

    InOrder inOrder = inOrder(orgSilServiceDaoMock);
    inOrder.verify(orgSilServiceDaoMock).findPaidNotificationOutcome("ORG_IPA", 2, 0);
    inOrder.verify(orgSilServiceDaoMock).findActualization("ORG_IPA", 2, 0);
    inOrder.verify(orgSilServiceDaoMock).findPaidNotificationOutcome("ORG_IPA", 2, 2);
    inOrder.verify(orgSilServiceDaoMock).findActualization("ORG_IPA", 2, 2);
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = new ExtractionRequest("ORG_IPA", MigrationFileType.ORG_SIL_SERVICES);
    OrgSilService first = orgSilService("first");
    OrgSilService second = orgSilService("second");
    when(orgSilServiceDaoMock.findPaidNotificationOutcome("ORG_IPA", 2, 0)).thenReturn(List.of(first));
    when(orgSilServiceDaoMock.findActualization("ORG_IPA", 2, 0)).thenReturn(List.of(second));
    when(orgSilServiceDaoMock.findPaidNotificationOutcome("ORG_IPA", 2, 2)).thenReturn(List.of());
    when(orgSilServiceDaoMock.findActualization("ORG_IPA", 2, 2)).thenReturn(List.of());
    when(orgSilServiceMapperMock.map(first)).thenReturn(dto("first"));
    when(orgSilServiceMapperMock.map(second)).thenReturn(dto("second"));

    ExportFileResult result = service.executeExport("BROKER_IPA", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("BROKER_IPA").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("ORG_IPA-ORG_SIL_SERVICES-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(orgSilServiceDaoMock);
    inOrder.verify(orgSilServiceDaoMock).findPaidNotificationOutcome("ORG_IPA", 2, 0);
    inOrder.verify(orgSilServiceDaoMock).findActualization("ORG_IPA", 2, 0);
    inOrder.verify(orgSilServiceDaoMock).findPaidNotificationOutcome("ORG_IPA", 2, 2);
    inOrder.verify(orgSilServiceDaoMock).findActualization("ORG_IPA", 2, 2);
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "BROKER_IPA",
      Map.of(MigrationFileType.ORG_SIL_SERVICES, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private OrgSilService orgSilService(String suffix) {
    return new OrgSilService(
      "ORG_IPA", "App" + suffix, "SERVICE_TYPE", "http://example.com",
      false, null, null, null, null, null, null, null, null
    );
  }

  private PuOrgSilServiceDTO dto(String suffix) {
    return PuOrgSilServiceDTO.builder()
      .ipaCode("ORG_IPA")
      .applicationName("App" + suffix)
      .serviceType("SERVICE_TYPE")
      .flagLegacy("false")
      .build();
  }

  private OrgSilService invalidOrgSilService() {
    return new OrgSilService(
      "not_valid", "", "", null,
      null, null, null, null, null, null, null, null, null
    );
  }

  private PuOrgSilServiceDTO invalidDto() {
    return PuOrgSilServiceDTO.builder()
      .ipaCode("not_valid")
      .applicationName("")
      .serviceType("")
      .flagLegacy("INVALID")
      .build();
  }
}
