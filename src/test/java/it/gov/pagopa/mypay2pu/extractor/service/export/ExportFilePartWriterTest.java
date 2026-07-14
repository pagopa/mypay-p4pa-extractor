package it.gov.pagopa.mypay2pu.extractor.service.export;

import com.opencsv.ICSVWriter;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportExecutionContext;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportPartResult;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportFilePartWriterTest {

  @Mock
  private CsvService csvServiceMock;

  @Mock
  private FileArchiverService fileArchiverServiceMock;

  @Mock
  private CsvRowErrorCollector errorCollectorMock;

  @InjectMocks
  private ExportFilePartWriter writer;

  @TempDir
  Path tempDir;

  @Test
  void givenExportContextWhenCreateErrorCollectorThenUseResolvedCsvPath() throws IOException {
    // Given
    ExportExecutionContext executionContext = executionContext();
    String exportBaseFileName = "export-file";
    Path expectedCsvPath = tempDir.resolve(exportBaseFileName + ".csv");
    Path expectedErrorFilePath = tempDir.resolve(exportBaseFileName + ".errors.csv");
    ICSVWriter csvWriterMock = mock(ICSVWriter.class);

    when(csvServiceMock.openCsvWriter(expectedErrorFilePath, true)).thenReturn(csvWriterMock);

    CsvRowErrorCollector errorCollector = writer.createErrorCollector(executionContext, exportBaseFileName);
    errorCollector.add(2, "field", "NotBlank", "must not be blank", "");

    // When
    Optional<Path> errorFilePath = errorCollector.writeToFile(expectedCsvPath);

    // Then
    assertTrue(errorFilePath.isPresent());
    assertEquals(expectedErrorFilePath, errorFilePath.orElseThrow());
  }

  @Test
  void givenValidRowsWhenWritePartThenCreateCsvZipAndReturnErrorFileName() throws IOException {
    // Given
    ExportExecutionContext executionContext = executionContext();
    String exportBaseFileName = "export-file";
    Path csvPath = tempDir.resolve(exportBaseFileName + ".csv");
    Path zipPath = tempDir.resolve(exportBaseFileName + ".zip");
    CsvRowErrorCollector errorCollector = errorCollectorMock;
    Supplier<List<TestDto>> csvRowsSupplier = () -> List.of(new TestDto("row-1"));

    when(errorCollectorMock.writeToFile(csvPath))
      .thenReturn(Optional.of(tempDir.resolve(exportBaseFileName + ".errors.csv")));
    when(fileArchiverServiceMock.zipFile(csvPath, zipPath)).thenReturn(zipPath);

    // When
    ExportPartResult result = writer.writePart(
      executionContext,
      exportBaseFileName,
      TestDto.class,
      csvRowsSupplier,
      errorCollector
    );

    // Then
    assertEquals(zipPath.getFileName().toString(), result.fileName());
    assertEquals(Optional.of("export-file.errors.csv"), result.errorFileName());
  }

  @Test
  void givenCsvWriteFailureWhenWritePartThenThrowIllegalStateException() throws IOException {
    // Given
    ExportExecutionContext executionContext = executionContext();
    String exportBaseFileName = "export-file";
    Path csvPath = tempDir.resolve(exportBaseFileName + ".csv");
    CsvRowErrorCollector errorCollector = errorCollectorMock;

    doNothing().when(errorCollectorMock).close();
    doThrow(new IOException("boom"))
      .when(csvServiceMock)
      .createCsv(eq(csvPath), eq(TestDto.class), any(), anyString());

    // When
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> writer.writePart(executionContext, exportBaseFileName, TestDto.class, () -> List.of(new TestDto("row-1")), errorCollector)
    );

    // Then
    assertEquals("Error writing to CSV file: boom", exception.getMessage());
  }

  @Test
  void givenArchivingFailureWhenWritePartThenThrowIllegalStateException() throws IOException {
    // Given
    ExportExecutionContext executionContext = executionContext();
    String exportBaseFileName = "export-file";
    Path csvPath = tempDir.resolve(exportBaseFileName + ".csv");
    Path zipPath = tempDir.resolve(exportBaseFileName + ".zip");
    CsvRowErrorCollector errorCollector = errorCollectorMock;

    doNothing().when(errorCollectorMock).close();
    doNothing().when(csvServiceMock).createCsv(eq(csvPath), eq(TestDto.class), any(), anyString());
    when(errorCollectorMock.writeToFile(csvPath)).thenReturn(Optional.empty());
    when(fileArchiverServiceMock.zipFile(csvPath, zipPath)).thenThrow(new IOException("zip-failed"));

    // When
    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> writer.writePart(executionContext, exportBaseFileName, TestDto.class, () -> List.of(new TestDto("row-1")), errorCollector)
    );

    // Then
    assertEquals(
      "Cannot archive files: " + csvPath + " into destination: " + zipPath,
      exception.getMessage()
    );
  }

  private ExportExecutionContext executionContext() {
    return new ExportExecutionContext(
      tempDir,
      it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.ORGANIZATIONS,
      "12345678901",
      "1_0",
      "20260101T010101",
      1000
    );
  }

  private record TestDto(String value) implements CsvExportDto {
  }
}
