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
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
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
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
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
    service = new OrganizationExportProcessingService(
      organizationDaoMock,
      organizationMapperMock,
      new CsvService(';', '"'),
      new FileArchiverService(false, "test-password", new it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService()),
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

    assertEquals(1, result.files().size());
    String exportFileName = result.files().get(0);
    assertTrue(exportFileName.matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.zip"));
    assertNull(result.error());
    Path archivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    assertTrue(Files.exists(archivePath));
    try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archivePath))) {
      String csvFileName = zipInputStream.getNextEntry().getName();
      assertTrue(csvFileName.matches("IPA_CODE-ORGANIZATIONS-\\d{14}-1_0\\.csv"));
    }
    InOrder inOrder = inOrder(organizationDaoMock);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 0);
    inOrder.verify(organizationDaoMock).findByFilters("IPA_CODE", null, 2, 2);
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
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
