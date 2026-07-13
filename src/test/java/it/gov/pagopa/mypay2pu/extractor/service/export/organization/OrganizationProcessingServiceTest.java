package it.gov.pagopa.mypay2pu.extractor.service.export.organization;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.organization.OrganizationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportBatchCoordinator;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportExecutionContext;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFilePartWriter;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportPartResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static it.gov.pagopa.mypay2pu.extractor.utils.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationProcessingServiceTest {

  @Mock
  private OrganizationDao organizationDaoMock;
  @Mock
  private OrganizationMapper organizationMapperMock;
  @Mock
  private ExportFileStatusDao exportFileStatusDaoMock;
  @Mock
  private ExportFilePartWriter exportFilePartWriterMock;

  private OrganizationProcessingService service;
  private ExtractionRequest request;
  private ExtractionFilters filters;
  private ExportExecutionContext executionContext;

  @BeforeEach
  void setUp() {
    ExportBatchCoordinator exportBatchCoordinator = new ExportBatchCoordinator();
    service = new OrganizationProcessingService(
      organizationDaoMock,
      organizationMapperMock,
      new ExtractorExportProperties(
        "C:\\exports",
        1024L,
        2,
        128,
        "12345678901"
      ),
      exportFileStatusDaoMock,
      exportBatchCoordinator,
      exportFilePartWriterMock
    );
    filters = new ExtractionFilters();
    request = new ExtractionRequest("IPA_CODE_TEST", MigrationFileType.ORGANIZATIONS, filters);
    executionContext = new ExportExecutionContext(
      Path.of("C:\\exports\\organizations"),
      MigrationFileType.ORGANIZATIONS,
      "12345678901",
      "1.0",
      "20260710173057",
      10,
      2
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      organizationDaoMock,
      organizationMapperMock,
      exportFileStatusDaoMock,
      exportFilePartWriterMock
    );
  }

  @Test
  void givenExtractionRequestWhenRetrieveDataThenDelegateToOrganizationDao() {
    Organization organization = buildOrganization();
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters)).thenReturn(List.of(organization));

    List<Organization> result = service.retrieveData(request);

    assertEquals(List.of(organization), result);
  }

  @Test
  void givenExecutionContextWhenRetrieveDataSupplierThenUseConfiguredPageSizeAndOffsets() {
    Organization first = buildOrganization();
    Organization second = buildOrganization();
    Organization third = buildOrganization();
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 2, 0)).thenReturn(List.of(first, second));
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 2, 2)).thenReturn(List.of(third));

    Supplier<List<Organization>> supplier = service.retrieveDataSupplier(request, executionContext);

    assertEquals(List.of(first, second), supplier.get());
    assertEquals(List.of(third), supplier.get());
    assertTrue(supplier.get().isEmpty());

  }

  @Test
  void givenOrganizationWhenAccessSubclassMetadataThenReturnExpectedValues() {
    Organization organization = buildOrganization();
    PuOrganizationDTO mappedOrganization = new PuOrganizationDTO();
    when(organizationMapperMock.map(organization)).thenReturn(mappedOrganization);

    assertEquals(MigrationFileType.ORGANIZATIONS, service.getMigrationFileType());
    assertSame(PuOrganizationDTO.class, service.getDtoClass());
    assertEquals(128L, service.getAvgRowSize());
    assertSame(mappedOrganization, service.toExportableEntity(organization));

  }

  @Test
  void givenNoDataWhenExtractThenGenerateSingleEmptyPart() {
    String extractionId = "extraction-id";
    Path extractionDirectory = Path.of("C:\\exports\\no-data");
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Supplier<List<PuOrganizationDTO>>> csvRowsSupplierCaptor =
      ArgumentCaptor.forClass(Supplier.class);
    ArgumentCaptor<ExportExecutionContext> executionContextCaptor =
      ArgumentCaptor.forClass(ExportExecutionContext.class);
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Class<PuOrganizationDTO>> dtoClassCaptor = ArgumentCaptor.forClass((Class) Class.class);

    when(exportFileStatusDaoMock.resolveExtractionDirectory(extractionId)).thenReturn(extractionDirectory);
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 2, 0)).thenReturn(List.of());
    doAnswer(invocation -> new ExportPartResult(
      invocation.getArgument(1, String.class) + ".zip",
      Optional.empty()
    )).when(exportFilePartWriterMock).writePart(
      executionContextCaptor.capture(),
      fileNameCaptor.capture(),
      dtoClassCaptor.capture(),
      csvRowsSupplierCaptor.capture()
    );

    ExportFileResult result = service.extract(extractionId, request);

    assertEquals(List.of(fileNameCaptor.getValue() + ".zip"), result.files());
    assertTrue(result.errorFiles().isEmpty());
    assertEquals(extractionDirectory, executionContextCaptor.getValue().extractionDirectory());
    assertEquals(MigrationFileType.ORGANIZATIONS, executionContextCaptor.getValue().migrationFileType());
    assertEquals("12345678901", executionContextCaptor.getValue().brokerCf());
    assertEquals("1_0", executionContextCaptor.getValue().zipVersion());
    assertEquals(8, executionContextCaptor.getValue().maxRowsPerPart());
    assertEquals(2, executionContextCaptor.getValue().pageSize());
    assertTrue(fileNameCaptor.getValue().startsWith("12345678901-ORGANIZATIONS-"));
    assertTrue(fileNameCaptor.getValue().endsWith("-1_0"));
    assertTrue(csvRowsSupplierCaptor.getValue().get().isEmpty());
    assertSame(PuOrganizationDTO.class, dtoClassCaptor.getValue());
  }

  @Test
  void givenMoreRowsThanPartLimitWhenExtractThenGenerateMultipleParts() {
    String extractionId = "extraction-id";
    Path extractionDirectory = Path.of("C:\\exports\\multipart");
    OrganizationProcessingService multipartService = new OrganizationProcessingService(
      organizationDaoMock,
      organizationMapperMock,
      new ExtractorExportProperties(
        "C:\\exports",
        4L,
        1,
        2,
        "12345678901"
      ),
      exportFileStatusDaoMock,
      new ExportBatchCoordinator(),
      exportFilePartWriterMock
    );
    Organization first = buildOrganization();
    Organization second = buildOrganization();
    Organization third = buildOrganization();
    PuOrganizationDTO firstDto = new PuOrganizationDTO();
    PuOrganizationDTO secondDto = new PuOrganizationDTO();
    PuOrganizationDTO thirdDto = new PuOrganizationDTO();
    List<List<List<PuOrganizationDTO>>> consumedPartBatches = new ArrayList<>();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Supplier<List<PuOrganizationDTO>>> csvRowsSupplierCaptor =
      ArgumentCaptor.forClass(Supplier.class);
    ArgumentCaptor<ExportExecutionContext> executionContextCaptor =
      ArgumentCaptor.forClass(ExportExecutionContext.class);
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Class<PuOrganizationDTO>> dtoClassCaptor = ArgumentCaptor.forClass((Class) Class.class);

    when(exportFileStatusDaoMock.resolveExtractionDirectory(extractionId)).thenReturn(extractionDirectory);
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 1, 0)).thenReturn(List.of(first));
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 1, 1)).thenReturn(List.of(second));
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 1, 2)).thenReturn(List.of(third));
    when(organizationDaoMock.findByFilters("IPA_CODE_TEST", filters, 1, 3)).thenReturn(List.of());
    when(organizationMapperMock.map(first)).thenReturn(firstDto);
    when(organizationMapperMock.map(second)).thenReturn(secondDto);
    when(organizationMapperMock.map(third)).thenReturn(thirdDto);
    doAnswer(invocation -> {
      String fileName = invocation.getArgument(1, String.class);
      @SuppressWarnings("unchecked")
      Supplier<List<PuOrganizationDTO>> supplier = invocation.getArgument(3, Supplier.class);
      List<List<PuOrganizationDTO>> consumedBatches = new ArrayList<>();
      List<PuOrganizationDTO> batch;
      while (!(batch = supplier.get()).isEmpty()) {
        consumedBatches.add(batch);
      }
      consumedPartBatches.add(consumedBatches);
      return new ExportPartResult(
        fileName + ".zip",
        fileName.contains("-part2-")
          ? Optional.of(fileName + ".errors.csv")
          : Optional.empty()
      );
    }).when(exportFilePartWriterMock).writePart(
      executionContextCaptor.capture(),
      fileNameCaptor.capture(),
      dtoClassCaptor.capture(),
      csvRowsSupplierCaptor.capture()
    );

    ExportFileResult result = multipartService.extract(extractionId, request);

    List<String> generatedBaseFileNames = fileNameCaptor.getAllValues();
    List<Supplier<List<PuOrganizationDTO>>> generatedSuppliers = csvRowsSupplierCaptor.getAllValues();

    assertEquals(
      List.of(
        generatedBaseFileNames.get(0) + ".zip",
        generatedBaseFileNames.get(1) + ".zip"
      ),
      result.files()
    );
    assertEquals(List.of(generatedBaseFileNames.get(1) + ".errors.csv"), result.errorFiles());
    assertEquals(2, generatedBaseFileNames.size());
    assertTrue(generatedBaseFileNames.get(0).contains("-part1-"));
    assertTrue(generatedBaseFileNames.get(1).contains("-part2-"));
    assertEquals(
      List.of(
        List.of(List.of(firstDto), List.of(secondDto)),
        List.of(List.of(thirdDto))
      ),
      consumedPartBatches
    );
    assertEquals(2, generatedSuppliers.size());
    assertEquals(
      List.of(PuOrganizationDTO.class, PuOrganizationDTO.class),
      dtoClassCaptor.getAllValues()
    );
    assertEquals(executionContextCaptor.getAllValues().get(0), executionContextCaptor.getAllValues().get(1));
  }

  @Test
  void givenMissingBrokerCfWhenBuildExecutionContextThenRejectConfiguration() {
    String extractionId = "extraction-id";
    Path extractionDirectory = Path.of("C:\\exports\\invalid-config");
    OrganizationProcessingService invalidService = new OrganizationProcessingService(
      organizationDaoMock,
      organizationMapperMock,
      new ExtractorExportProperties(
        "C:\\exports",
        4L,
        1,
        2,
        " "
      ),
      exportFileStatusDaoMock,
      new ExportBatchCoordinator(),
      exportFilePartWriterMock
    );

    when(exportFileStatusDaoMock.resolveExtractionDirectory(extractionId)).thenReturn(extractionDirectory);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> invalidService.buildExecutionContext(extractionId, MigrationFileType.ORGANIZATIONS, 2L)
    );

    assertEquals("Missing required brokerCf configuration for ZIP naming", exception.getMessage());
  }
}
