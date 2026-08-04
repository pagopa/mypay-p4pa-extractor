package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.service.files.TestCsv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvPartitionWriterServiceTest {

  @TempDir
  Path tempDir;

  private final CsvPartitionWriterService service = new CsvPartitionWriterService(new CsvService(';', '"'));

  @Test
  void whenRowsExceedThresholdThenWriteMultiplePartsWithDeterministicNames() throws IOException {
    Supplier<List<TestCsv>> supplier = oneShotSupplier(List.of(
      testCsv("A1", "B1"),
      testCsv("A2", "B2"),
      testCsv("A3", "B3")
    ));

    List<Path> generatedPaths = service.writeCsv(tempDir, getFileNameBuilder(), TestCsv.class, supplier, "1_0", 2);

    assertEquals(2, generatedPaths.size());
    assertEquals(getFileNameBuilder().buildCsvPartFileName(1), generatedPaths.get(0).getFileName().toString());
    assertEquals(getFileNameBuilder().buildCsvPartFileName(2), generatedPaths.get(1).getFileName().toString());
    assertTrue(Files.exists(generatedPaths.get(0)));
    assertTrue(Files.exists(generatedPaths.get(1)));
    assertEquals(3, Files.readAllLines(generatedPaths.get(0)).size());
    assertEquals(2, Files.readAllLines(generatedPaths.get(1)).size());
  }

  @Test
  void whenRowsDoNotExceedThresholdThenKeepOriginalFileName() throws IOException {
    Path csvFilePath = tempDir.resolve(getFileNameBuilder().buildCsvFileName());
    Supplier<List<TestCsv>> supplier = oneShotSupplier(List.of(
      testCsv("A1", "B1"),
      testCsv("A2", "B2")
    ));

    List<Path> generatedPaths = service.writeCsv(tempDir, getFileNameBuilder(), TestCsv.class, supplier, "1_0", 2);

    assertEquals(1, generatedPaths.size());
    assertEquals(csvFilePath, generatedPaths.get(0));
    assertTrue(Files.exists(csvFilePath));
  }

  @Test
  void whenNoRowsThenCreateSingleEmptyBaseFile() throws IOException {
    Path csvFilePath = tempDir.resolve(getFileNameBuilder().buildCsvFileName());

    List<Path> generatedPaths = service.writeCsv(tempDir, getFileNameBuilder(), TestCsv.class, List::of, "1_0", 2);

    assertEquals(1, generatedPaths.size());
    assertEquals(csvFilePath, generatedPaths.get(0));
    assertTrue(Files.exists(csvFilePath));
  }

  @Test
  void whenThresholdIsNotPositiveThenFailFast() {
    ExportFileNameBuilder fileNameBuilder = getFileNameBuilder();
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> {
        service.writeCsv(tempDir, fileNameBuilder, TestCsv.class, List::of, "1_0", 0);
      }
    );
    assertEquals("Max rows per part must be positive", exception.getMessage());
  }

  private Supplier<List<TestCsv>> oneShotSupplier(List<TestCsv> rows) {
    AtomicBoolean delivered = new AtomicBoolean(false);
    return () -> {
      if (delivered.get()) {
        return List.of();
      }
      delivered.set(true);
      return rows;
    };
  }

  private TestCsv testCsv(String column1, String column2) {
    return TestCsv.builder()
      .column1(column1)
      .column2(column2)
      .column3(LocalDate.of(2026, Month.JANUARY, 1))
      .build();
  }

  private ExportFileNameBuilder getFileNameBuilder() {
    return new ExportFileNameBuilder("IPA_CODE", "IPA_CODE_ORG", true, MigrationFileType.ORGANIZATIONS, LocalDate.now().atStartOfDay(), "1_0");
  }
}
