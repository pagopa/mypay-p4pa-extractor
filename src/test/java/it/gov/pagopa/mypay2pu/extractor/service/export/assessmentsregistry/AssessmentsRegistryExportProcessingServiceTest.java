package it.gov.pagopa.mypay2pu.extractor.service.export.assessmentsregistry;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.AssessmentsRegistryDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsRegistryDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.assessmentsregistry.AssessmentsRegistryMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.AssessmentsRegistry;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private AssessmentsRegistryDao assessmentsRegistryDaoMock;

  @Mock
  private AssessmentsRegistryMapper assessmentsRegistryMapperMock;

  private AssessmentsRegistryExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new AssessmentsRegistryExportProcessingService(
      assessmentsRegistryDaoMock,
      assessmentsRegistryMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(assessmentsRegistryDaoMock, assessmentsRegistryMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedAssessmentsRegistryToArchive() throws Exception {
    ExtractionRequest request = request("TYPE-1,TYPE-2");
    AssessmentsRegistry first = assessmentsRegistry("first");
    AssessmentsRegistry second = invalidAssessmentsRegistry();
    PuAssessmentsRegistryDTO firstDto = dto("first");
    PuAssessmentsRegistryDTO secondDto = invalidDto();
    when(assessmentsRegistryDaoMock.findByFilters(
      "IPA_CODE", null, List.of("TYPE-1", "TYPE-2"), null, null, 2, 0
    )).thenReturn(List.of(first, second));
    when(assessmentsRegistryDaoMock.findByFilters(
      "IPA_CODE", null, List.of("TYPE-1", "TYPE-2"), null, null, 2, 2
    )).thenReturn(List.of());
    when(assessmentsRegistryMapperMock.map(first)).thenReturn(firstDto);
    when(assessmentsRegistryMapperMock.map(second)).thenReturn(secondDto);

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
    assertTrue(exportFileName.matches("IPA_CODE-ASSESSMENTS_REGISTRY-\\d{14}-1_0\\.zip"));
    assertTrue(errorFileName.matches("IPA_CODE-ASSESSMENTS_REGISTRY-\\d{14}-1_0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("IPA_CODE").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));
    assertEquals(1, ZipUtils.readZipEntries(exportArchivePath).size());
    assertEquals(1, ZipUtils.readZipEntries(errorArchivePath).size());

    InOrder inOrder = inOrder(assessmentsRegistryDaoMock);
    inOrder.verify(assessmentsRegistryDaoMock).findByFilters(
      "IPA_CODE", null, List.of("TYPE-1", "TYPE-2"), null, null, 2, 0
    );
    inOrder.verify(assessmentsRegistryDaoMock).findByFilters(
      "IPA_CODE", null, List.of("TYPE-1", "TYPE-2"), null, null, 2, 2
    );
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = request(null);
    AssessmentsRegistry first = assessmentsRegistry("first");
    AssessmentsRegistry second = assessmentsRegistry("second");
    when(assessmentsRegistryDaoMock.findByFilters("IPA_CODE", null, List.of(), null, null, 2, 0))
      .thenReturn(List.of(first, second));
    when(assessmentsRegistryDaoMock.findByFilters("IPA_CODE", null, List.of(), null, null, 2, 2))
      .thenReturn(List.of());
    when(assessmentsRegistryMapperMock.map(first)).thenReturn(dto("first"));
    when(assessmentsRegistryMapperMock.map(second)).thenReturn(dto("second"));

    ExportFileResult result = service.executeExport("IPA_CODE", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("IPA_CODE").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("IPA_CODE-ASSESSMENTS_REGISTRY-\\d{14}-1_0\\.csv"));

    InOrder inOrder = inOrder(assessmentsRegistryDaoMock);
    inOrder.verify(assessmentsRegistryDaoMock).findByFilters(
      "IPA_CODE", null, List.of(), null, null, 2, 0
    );
    inOrder.verify(assessmentsRegistryDaoMock).findByFilters(
      "IPA_CODE", null, List.of(), null, null, 2, 2
    );
  }

  @Test
  void retrieveDataShouldDelegateAllFiltersToDao() {
    OffsetDateTime lastExtractionDate = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime dateFrom = OffsetDateTime.parse("2026-01-10T00:00:00Z");
    OffsetDateTime dateTo = OffsetDateTime.parse("2026-01-11T00:00:00Z");
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.ASSESSMENTS_REGISTRY,
      lastExtractionDate,
      new ExtractionFilters()
        .logicalKey("TYPE-1,TYPE-2")
        .dateFrom(dateFrom)
        .dateTo(dateTo)
    );
    List<AssessmentsRegistry> expected = List.of();
    when(assessmentsRegistryDaoMock.findByFilters(
      "IPA_CODE",
      lastExtractionDate,
      List.of("TYPE-1", "TYPE-2"),
      dateFrom,
      dateTo,
      50,
      100
    )).thenReturn(expected);

    assertEquals(expected, service.retrieveData("IPA_CODE", request, 50, 100));

    verify(assessmentsRegistryDaoMock).findByFilters(
      "IPA_CODE",
      lastExtractionDate,
      List.of("TYPE-1", "TYPE-2"),
      dateFrom,
      dateTo,
      50,
      100
    );
  }

  private ExtractionRequest request(String logicalKey) {
    return new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.ASSESSMENTS_REGISTRY,
      null,
      new ExtractionFilters().logicalKey(logicalKey)
    );
  }

  private ExtractorExportProperties exportProperties() {
    return new ExtractorExportProperties(
      tempDir.toString(),
      tempDir.toString(),
      "12345678901",
      "IPA_CODE",
      Map.of(MigrationFileType.ASSESSMENTS_REGISTRY, new ExtractorExportProperties.FileTypeConfiguration(2)),
      new ExtractorExportProperties.PaymentsReportingConfiguration(tempDir.toString())
    );
  }

  private AssessmentsRegistry assessmentsRegistry(String suffix) {
    return new AssessmentsRegistry(
      "IPA_CODE",
      "TYPE_" + suffix,
      "SECTION_" + suffix,
      "Section " + suffix,
      "OFFICE_" + suffix,
      "Office " + suffix,
      "CODE_" + suffix,
      "Description " + suffix,
      "2026",
      "true"
    );
  }

  private AssessmentsRegistry invalidAssessmentsRegistry() {
    return new AssessmentsRegistry(null, null, null, null, null, null, null, null, null, null);
  }

  private PuAssessmentsRegistryDTO dto(String suffix) {
    return PuAssessmentsRegistryDTO.builder()
      .organizationIpaCode("IPA_CODE")
      .debtPositionTypeOrgCode("TYPE_" + suffix)
      .sectionCode("SECTION_" + suffix)
      .sectionDescription("Section " + suffix)
      .officeCode("OFFICE_" + suffix)
      .officeDescription("Office " + suffix)
      .assessmentCode("CODE_" + suffix)
      .assessmentDescription("Description " + suffix)
      .operatingYear("2026")
      .status("ACTIVE")
      .build();
  }

  private PuAssessmentsRegistryDTO invalidDto() {
    return PuAssessmentsRegistryDTO.builder().build();
  }
}
