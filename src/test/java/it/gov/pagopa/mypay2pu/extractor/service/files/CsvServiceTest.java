package it.gov.pagopa.mypay2pu.extractor.service.files;

import it.gov.pagopa.mypay2pu.extractor.exception.InvalidCsvRowException;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class CsvServiceTest {

    private final PodamFactory podamFactory = new PodamFactoryImpl();
    private final CsvService csvService = new CsvService(';', '\"');

    @Test
    void testCreateCsv_success() throws IOException {
        // Give
        Path filePath = Path.of("build", "tmp", "test", "output.csv");

        String[] headerArray = new String[]{"Header1", "Header2"};
        List<String[]> header = new ArrayList<>(List.of());
        header.add(headerArray);
        List<String[]> data = Arrays.asList(new String[]{"Data1", "Data2"}, new String[]{"Data3", "Data4"});

        // When
        csvService.createCsv(filePath, header, data);

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
    }

    @Test
    void testCreateCsv_noData() throws IOException {
        // Give
        Path filePath = Path.of("build", "tmp", "test", "empty.csv");
        String[] headerArray = new String[]{"Header1", "Header2"};
        List<String[]> header = new ArrayList<>(List.of());
        header.add(headerArray);
        List<String[]> data = List.of();

        // When
        csvService.createCsv(filePath, header, data);

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
    }

    @Test
    void testCreateCsv_noHeader() throws IOException {
        // Give
        Path filePath = Path.of("build", "tmp", "test", "no_header.csv");
        List<String[]> header = List.of();
        List<String[]> data = Arrays.asList(new String[]{"Data1", "Data2"}, new String[]{"Data3", "Data4"});

        // When
        csvService.createCsv(filePath, header, data);

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
    }


    @Test
    void testCreateCsvFromBean_successWithRepeatedSupplier() throws IOException {
        // Given
        Path filePath = Path.of("build", "tmp", "test", "EXPORT.csv");

        TestCsv testCsv = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv1 = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv2 = podamFactory.manufacturePojo(TestCsv.class);

        List<TestCsv> testCsvList = List.of(testCsv, testCsv1, testCsv2);
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        Supplier<List<TestCsv>> csvRowsSupplier = () -> {
            if (supplierCalled.get()) {
                return Collections.emptyList();
            }
            supplierCalled.set(true);
            return testCsvList;
        };

        // When
        csvService.createCsv(filePath, TestCsv.class, csvRowsSupplier, "v1");

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
    }

    @Test
    void testCreateCsvFromBean_nullValueIsWrittenAsUnquotedEmptyField() throws IOException {
        // Given
        Path filePath = Path.of("build", "tmp", "test", "EXPORT_NULLS.csv");
        TestCsv testCsv = TestCsv.builder()
          .column1("Data1")
          .column2("Data2")
          .column3(null)
          .build();
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        // When
        csvService.createCsv(filePath, TestCsv.class, () -> {
            if (supplierCalled.get()) {
                return Collections.emptyList();
            }
            supplierCalled.set(true);
            return List.of(testCsv);
        }, "v1");

        // Then
        List<String> rows = Files.readAllLines(filePath);
        assertEquals(2, rows.size(), "The csv should contain header and one data row.");
        assertEquals("Data1;Data2;", rows.get(1), "Null values should be written as unquoted empty fields.");
    }

    @Test
    void testCreateCsvFromBean_jsonValueEscapesDoubleQuotes() throws IOException {
        Path filePath = Path.of("build", "tmp", "test", "EXPORT_JSON.csv");
        TestCsv testCsv = TestCsv.builder()
          .column1("Data1")
          .column2("{\"fieldBeans\":[]}")
          .column3(LocalDate.of(2026, Month.JANUARY, 1))
          .build();
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        csvService.createCsv(filePath, TestCsv.class, () -> {
            if (supplierCalled.get()) {
                return Collections.emptyList();
            }
            supplierCalled.set(true);
            return List.of(testCsv);
        }, "v1");

        List<String> rows = Files.readAllLines(filePath);

        assertEquals("Data1;\"{\"\"fieldBeans\"\":[]}\";2026-01-01", rows.get(1));
    }

    @Test
    void testCreateCsv_whenCsvRequiredFieldEmptyException_thenThrowInvalidCsvRowException() {
        // Given
        Path filePath = Path.of("build", "tmp", "test", "EXPORT.csv");

        TestCsv testCsv = podamFactory.manufacturePojo(TestCsv.class);
        testCsv.setColumn1(null);
        List<TestCsv> testCsvList = List.of(testCsv);

        // When / Then
        InvalidCsvRowException ex = assertThrows(InvalidCsvRowException.class, () -> csvService.createCsv(filePath, TestCsv.class, () -> testCsvList, "v1"));

        assertEquals("Invalid CSV row: Field 'column1' is mandatory but no value was provided.", ex.getMessage());

    }

}
