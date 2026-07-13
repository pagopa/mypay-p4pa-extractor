package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFilePartWriterTest {

  @Mock
  private CsvService csvServiceMock;
  @Mock
  private Validator validatorMock;
  @Mock
  private FileArchiverService fileArchiverServiceMock;

  @TempDir
  private Path tempDir;

  private ExportFilePartWriter service;
  private ExportExecutionContext executionContext;

  @BeforeEach
  void setUp() {
    service = new ExportFilePartWriter(csvServiceMock, validatorMock, fileArchiverServiceMock);
    executionContext = new ExportExecutionContext(
      tempDir,
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
    verifyNoMoreInteractions(csvServiceMock, validatorMock, fileArchiverServiceMock);
  }

  @Test
  void givenValidRowsWhenWritePartThenCreateCsvArchiveAndDeleteTemporaryCsv() throws IOException {
    String exportBaseFileName = "organizations-export";
    Path csvPath = executionContext.resolveCsvPath(exportBaseFileName);
    Path zipPath = executionContext.resolveZipPath(exportBaseFileName);
    TestDto row = new TestDto("row-1");
    AtomicBoolean consumed = new AtomicBoolean(false);
    Supplier<List<TestDto>> csvRowsSupplier = () -> consumed.compareAndSet(false, true) ? List.of(row) : List.of();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Supplier<List<TestDto>>> validatingSupplierCaptor = ArgumentCaptor.forClass(Supplier.class);

    when(validatorMock.validate(row)).thenReturn(Set.of());
    when(fileArchiverServiceMock.zipFile(csvPath, zipPath)).thenReturn(zipPath);
    doAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      Supplier<List<TestDto>> validatingSupplier = invocation.getArgument(2, Supplier.class);
      assertEquals(List.of(row), validatingSupplier.get());
      assertTrue(validatingSupplier.get().isEmpty());
      Files.createFile(csvPath);
      return null;
    }).when(csvServiceMock).createCsv(
      eq(csvPath),
      eq(TestDto.class),
      validatingSupplierCaptor.capture(),
      eq(executionContext.zipVersion())
    );

    ExportPartResult result = service.writePart(
      executionContext,
      exportBaseFileName,
      TestDto.class,
      csvRowsSupplier
    );

    assertEquals(
      new ExportPartResult("organizations-export.zip", Optional.empty()),
      result
    );
    assertFalse(Files.exists(csvPath));

    then(csvServiceMock).should().createCsv(
      csvPath,
      TestDto.class,
      validatingSupplierCaptor.getValue(),
      executionContext.zipVersion()
    );
    then(validatorMock).should().validate(row);
    then(fileArchiverServiceMock).should().zipFile(csvPath, zipPath);
  }

  @Test
  void givenCsvCreationFailureWhenWritePartThenThrowIllegalStateException() throws IOException {
    String exportBaseFileName = "organizations-export";
    Path csvPath = executionContext.resolveCsvPath(exportBaseFileName);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Supplier<List<TestDto>>> validatingSupplierCaptor = ArgumentCaptor.forClass(Supplier.class);

    doThrow(new IOException("csv broken")).when(csvServiceMock).createCsv(
      eq(csvPath),
      eq(TestDto.class),
      validatingSupplierCaptor.capture(),
      eq(executionContext.zipVersion())
    );

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> service.writePart(executionContext, exportBaseFileName, TestDto.class, List::of)
    );

    assertEquals("Error writing to CSV file: csv broken", exception.getMessage());

    then(csvServiceMock).should().createCsv(
      csvPath,
      TestDto.class,
      validatingSupplierCaptor.getValue(),
      executionContext.zipVersion()
    );
  }

  @Test
  void givenArchiveFailureWhenWritePartThenThrowIllegalStateException() throws IOException {
    String exportBaseFileName = "organizations-export";
    Path csvPath = executionContext.resolveCsvPath(exportBaseFileName);
    Path zipPath = executionContext.resolveZipPath(exportBaseFileName);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Supplier<List<TestDto>>> validatingSupplierCaptor = ArgumentCaptor.forClass(Supplier.class);

    doAnswer(invocation -> {
      Files.createFile(csvPath);
      return null;
    }).when(csvServiceMock).createCsv(
      eq(csvPath),
      eq(TestDto.class),
      validatingSupplierCaptor.capture(),
      eq(executionContext.zipVersion())
    );
    doThrow(new IOException("zip broken")).when(fileArchiverServiceMock).zipFile(csvPath, zipPath);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> service.writePart(executionContext, exportBaseFileName, TestDto.class, List::of)
    );

    assertEquals(
      "Cannot archive files: " + csvPath + " into destination: " + zipPath,
      exception.getMessage()
    );

    then(csvServiceMock).should().createCsv(
      csvPath,
      TestDto.class,
      validatingSupplierCaptor.getValue(),
      executionContext.zipVersion()
    );
    then(fileArchiverServiceMock).should().zipFile(csvPath, zipPath);
  }

  private record TestDto(String value) implements CsvExportDto {
  }
}
