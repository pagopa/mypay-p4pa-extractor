package it.gov.pagopa.mypay2pu.extractor.service.export.assessments;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.AssessmentsDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.assessments.AssessmentsMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
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
class AssessmentsExportProcessingServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private AssessmentsDao assessmentsDaoMock;

  @Mock
  private AssessmentsMapper assessmentsMapperMock;

  private AssessmentsExportProcessingService service;

  @BeforeEach
  void setUp() {
    CsvService csvService = new CsvService(';', '"');
    service = new AssessmentsExportProcessingService(
      assessmentsDaoMock,
      assessmentsMapperMock,
      csvService,
      new CsvPartitionWriterService(csvService),
      new FileArchiverService(false, "test-password", new ZipFileService()),
      Validation.buildDefaultValidatorFactory().getValidator(),
      exportProperties()
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(assessmentsDaoMock, assessmentsMapperMock);
  }

  @Test
  void whenDataIsAvailableThenExportPagedAssessmentsToArchive() throws Exception {
    ExtractionRequest request = request("ASSESSMENT-1,ASSESSMENT-2");
    Assessments first = assessments("first");
    Assessments second = invalidAssessments();
    PuAssessmentsDTO firstDto = dto("first");
    PuAssessmentsDTO secondDto = invalidDto();
    when(assessmentsDaoMock.findByFilters(
      "IPA_CODE", null, List.of("ASSESSMENT-1", "ASSESSMENT-2"), null, null, 2, 0
    )).thenReturn(List.of(first, second));
    when(assessmentsDaoMock.findByFilters(
      "IPA_CODE", null, List.of("ASSESSMENT-1", "ASSESSMENT-2"), null, null, 2, 2
    )).thenReturn(List.of());
    when(assessmentsMapperMock.map(first)).thenReturn(firstDto);
    when(assessmentsMapperMock.map(second)).thenReturn(secondDto);

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
    assertTrue(exportFileName.matches("IPA_CODE-ASSESSMENTS-\\d{14}-1\\.0\\.zip"));
    assertTrue(errorFileName.matches("IPA_CODE-ASSESSMENTS-\\d{14}-1\\.0\\.errors\\.zip"));

    Path exportArchivePath = tempDir.resolve("IPA_CODE").resolve(exportFileName);
    Path errorArchivePath = tempDir.resolve("IPA_CODE").resolve(errorFileName);
    assertTrue(Files.exists(exportArchivePath));
    assertTrue(Files.exists(errorArchivePath));
    assertEquals(1, ZipUtils.readZipEntries(exportArchivePath).size());
    assertEquals(1, ZipUtils.readZipEntries(errorArchivePath).size());

    InOrder inOrder = inOrder(assessmentsDaoMock);
    inOrder.verify(assessmentsDaoMock).findByFilters(
      "IPA_CODE", null, List.of("ASSESSMENT-1", "ASSESSMENT-2"), null, null, 2, 0
    );
    inOrder.verify(assessmentsDaoMock).findByFilters(
      "IPA_CODE", null, List.of("ASSESSMENT-1", "ASSESSMENT-2"), null, null, 2, 2
    );
  }

  @Test
  void whenNoValidationErrorsThenArchiveContainsOnlyExportCsv() throws Exception {
    ExtractionRequest request = request(null);
    Assessments first = assessments("first");
    Assessments second = assessments("second");
    when(assessmentsDaoMock.findByFilters("IPA_CODE", null, List.of(), null, null, 2, 0))
      .thenReturn(List.of(first, second));
    when(assessmentsDaoMock.findByFilters("IPA_CODE", null, List.of(), null, null, 2, 2))
      .thenReturn(List.of());
    when(assessmentsMapperMock.map(first)).thenReturn(dto("first"));
    when(assessmentsMapperMock.map(second)).thenReturn(dto("second"));

    ExportFileResult result = service.executeExport("IPA_CODE", request);

    assertNull(result.error());
    assertEquals(1, result.files().size());
    Path archivePath = tempDir.resolve("IPA_CODE").resolve(result.files().get(0));
    List<String> archiveEntries = ZipUtils.readZipEntries(archivePath);
    assertEquals(1, archiveEntries.size());
    assertTrue(archiveEntries.get(0).matches("IPA_CODE-ASSESSMENTS-\\d{14}-1\\.0\\.csv"));

    InOrder inOrder = inOrder(assessmentsDaoMock);
    inOrder.verify(assessmentsDaoMock).findByFilters("IPA_CODE", null, List.of(), null, null, 2, 0);
    inOrder.verify(assessmentsDaoMock).findByFilters("IPA_CODE", null, List.of(), null, null, 2, 2);
  }

  @Test
  void retrieveDataShouldDelegateAllFiltersToDao() {
    OffsetDateTime lastExtractionDate = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    OffsetDateTime dateFrom = OffsetDateTime.parse("2026-01-10T00:00:00Z");
    OffsetDateTime dateTo = OffsetDateTime.parse("2026-01-11T00:00:00Z");
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.ASSESSMENTS,
      lastExtractionDate,
      new ExtractionFilters()
        .logicalKey("ASSESSMENT-1,ASSESSMENT-2")
        .dateFrom(dateFrom)
        .dateTo(dateTo)
    );
    List<Assessments> expected = List.of();
    when(assessmentsDaoMock.findByFilters(
      "IPA_CODE",
      lastExtractionDate,
      List.of("ASSESSMENT-1", "ASSESSMENT-2"),
      dateFrom,
      dateTo,
      50,
      100
    )).thenReturn(expected);

    assertEquals(expected, service.retrieveData("IPA_CODE", request, 50, 100));

    verify(assessmentsDaoMock).findByFilters(
      "IPA_CODE",
      lastExtractionDate,
      List.of("ASSESSMENT-1", "ASSESSMENT-2"),
      dateFrom,
      dateTo,
      50,
      100
    );
  }

  private ExtractionRequest request(String logicalKey) {
    return new ExtractionRequest(
      List.of("IPA_CODE"),
      MigrationFileType.ASSESSMENTS,
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
      Map.of(MigrationFileType.ASSESSMENTS, new ExtractorExportProperties.FileTypeConfiguration(2))
    );
  }

  private Assessments assessments(String suffix) {
    return new Assessments(
      "Assessment " + suffix,
      "IPA_CODE",
      "TYPE_" + suffix,
      "IUV_" + suffix,
      "IUD_" + suffix,
      "OFFICE_" + suffix,
      "Office " + suffix,
      "SECTION_" + suffix,
      "Section " + suffix,
      "CODE_" + suffix,
      "Description " + suffix,
      100L,
      false
    );
  }

  private PuAssessmentsDTO dto(String suffix) {
    return PuAssessmentsDTO.builder()
      .assessmentName("Assessment " + suffix)
      .organizationIpaCode("IPA_CODE")
      .debtPositionTypeOrgCode("TYPE_" + suffix)
      .iuv("IUV_" + suffix)
      .iud("IUD_" + suffix)
      .officeCode("OFFICE_" + suffix)
      .officeDescription("Office " + suffix)
      .sectionCode("SECTION_" + suffix)
      .sectionDescription("Section " + suffix)
      .assessmentCode("CODE_" + suffix)
      .assessmentDescription("Description " + suffix)
      .amountCents(100L)
      .amountSubmitted(false)
      .build();
  }

  private Assessments invalidAssessments() {
    return new Assessments(null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  private PuAssessmentsDTO invalidDto() {
    return PuAssessmentsDTO.builder().build();
  }
}
