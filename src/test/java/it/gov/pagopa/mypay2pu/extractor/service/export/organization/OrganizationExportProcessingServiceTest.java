package it.gov.pagopa.mypay2pu.extractor.service.export.organization;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.organization.OrganizationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.ZipUtils;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private OrganizationDao organizationDaoMock;
  @Mock
  private OrganizationMapper organizationMapperMock;

  private OrganizationExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new OrganizationExportProcessingService(
      organizationDaoMock,
      organizationMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(organizationDaoMock, organizationMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedOrganizationsToArchive() throws Exception {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.ORGANIZATIONS);
    Organization first = organization("first");
    Organization second = invalidOrganization();
    PuOrganizationDTO firstDto = dto("first");
    PuOrganizationDTO secondDto = invalidDto();
    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first, second));
    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 2)).thenReturn(List.of());
    when(organizationMapperMock.map(first)).thenReturn(firstDto);
    when(organizationMapperMock.map(second)).thenReturn(secondDto);

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
    assertTrue(exportFileName.matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("IPA_CODE").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(1, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.csv"));

    List<String> errorArchiveEntries = ZipUtils.readZipEntries(errorArchivePath);
    assertEquals(1, errorArchiveEntries.size());
    assertTrue(errorArchiveEntries.get(0).matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.errors\\.csv"));
    InOrder inOrder = inOrder(organizationDaoMock);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.ORGANIZATIONS);
    Organization first = organization("first");
    Organization second = organization("second");
    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first, second));
    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 2)).thenReturn(List.of());
    when(organizationMapperMock.map(first)).thenReturn(dto("first"));
    when(organizationMapperMock.map(second)).thenReturn(dto("second"));

    ExportFileResult result = service.executeExport("IPA_CODE", request);

    assertNull(result.error());
    Path archivePath = tempDir.resolve("IPA_CODE").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(organizationDaoMock);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  @Test
  void whenRowsExceedThresholdThenArchiveContainsCsvPartsInOrder() throws Exception {
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.ORGANIZATIONS);
    Organization first = organization("first");
    Organization second = organization("second");
    Organization third = organization("third");

    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first, second));
    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 2)).thenReturn(List.of(third));
    when(organizationMapperMock.map(first)).thenReturn(dto("first"));
    when(organizationMapperMock.map(second)).thenReturn(dto("second"));
    when(organizationMapperMock.map(third)).thenReturn(dto("third"));

    ExportFileResult result = service.executeExport("IPA_CODE", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    String exportFileName = result.files().get(0);
    assertTrue(exportFileName.matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.zip"));

    Path exportArchivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    assertTrue(Files.exists(exportArchivePath));

    List<String> exportArchiveEntries = ZipUtils.readZipEntries(exportArchivePath);
    assertEquals(2, exportArchiveEntries.size());
    assertTrue(exportArchiveEntries.get(0).matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0_part_001\\.csv"));
    assertTrue(exportArchiveEntries.get(1).matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0_part_002\\.csv"));

    InOrder inOrder = inOrder(organizationDaoMock);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  @Test
  void whenRowMappingFailsThenWorkingDirectoryIsCleanedUp() {
    String extractionId = "EXTRACTION_ID";
    ExtractionRequest request = new ExtractionRequest("IPA_CODE", MigrationFileType.ORGANIZATIONS);
    Organization first = organization("first");

    when(organizationDaoMock.findByFilters("IPA_CODE", null, 2, 0)).thenReturn(List.of(first));
    when(organizationMapperMock.map(first)).thenThrow(new RuntimeException("mapping failure"));

    assertThrows(RuntimeException.class, () -> service.executeExport(extractionId, request));
    assertFalse(Files.exists(tempDir.resolve(extractionId).resolve("organizations")));
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "IPA_CODE",
      Map.of(MigrationFileType.ORGANIZATIONS, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private Organization organization(String suffix) {
    return new Organization(
      "IPA_CODE", "12345678901", suffix, "TYPE", "mail@example.com",
      null, null, null, null, null, "ACTIVE", null, null, false, false, false, null
    );
  }

  private PuOrganizationDTO dto(String suffix) {
    return PuOrganizationDTO.builder()
      .ipaCode("IPA_CODE")
      .orgFiscalCode("12345678901")
      .orgName(suffix)
      .flagNotifyIo(false)
      .flagNotifyOutcomePush(false)
      .status("ACTIVE")
      .brokerCf("12345678901")
      .brokerIpaCode("IPA_CODE")
      .flagTreasury("false")
      .build();
  }

  private Organization invalidOrganization() {
    return new Organization(
      "IPA_CODE", "NOT_A_FISCAL_CODE", "", "TYPE", "not-an-email",
      "invalid-iban", null, "999", "12", null, "ACTIVE", "TOOLONG",
      null, false, false, false, null
    );
  }

  private PuOrganizationDTO invalidDto() {
    return PuOrganizationDTO.builder()
      .ipaCode("IPA_CODE")
      .orgFiscalCode("NOT_A_FISCAL_CODE")
      .orgName("")
      .orgTypeCode("TYPE")
      .orgEmail("not-an-email")
      .iban("invalid-iban")
      .segregationCode("999")
      .cbillInterBankCode("12")
      .additionalLanguage("TOOLONG")
      .flagNotifyIo(false)
      .flagNotifyOutcomePush(false)
      .status("ACTIVE")
      .brokerCf("12345678901")
      .brokerIpaCode("IPA_CODE")
      .flagTreasury("false")
      .build();
  }
}
