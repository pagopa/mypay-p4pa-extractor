package it.gov.pagopa.mypay2pu.extractor.service.files;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.mypay2pu.extractor.exception.InvalidCsvRowException;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class CsvServiceTest {

    private final PodamFactory podamFactory = new PodamFactoryImpl();
    private final CsvService csvService = new CsvService(';', '\"', 5, 10);

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
    void testCreateCsvFromBean_success() throws IOException {
        // Give
        Path filePath = Path.of("build", "tmp", "test", "EXPORT.csv");

        TestCsv testCsv = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv1 = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv2 = podamFactory.manufacturePojo(TestCsv.class);

        List<TestCsv> testCsvList = List.of(testCsv, testCsv1, testCsv2);
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        Supplier<List<TestCsv>> csvRowsSupplier = () -> {
            if (supplierCalled.get()) {
                return Collections.emptyList();
            } else {
                supplierCalled.set(true);
                return testCsvList;
            }
        };
        // When
        csvService.createCsv(filePath, TestCsv.class, csvRowsSupplier, "v1");

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
    }


    @Test
    void testCreateCsvFromBean_whenPageRequestCountBiggerThenThreshold_thenThrowIllegalException() {
        // Given
        Path filePath = Path.of("build", "tmp", "test", "EXPORT.csv");

        TestCsv testCsv = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv1 = podamFactory.manufacturePojo(TestCsv.class);
        TestCsv testCsv2 = podamFactory.manufacturePojo(TestCsv.class);

        List<TestCsv> testCsvList = List.of(testCsv, testCsv1, testCsv2);

        // When
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                csvService.createCsv(filePath, TestCsv.class, () -> testCsvList, "v1"));

        // Then
        File file = filePath.toFile();
        assertTrue(file.exists(), "The file should exist.");
        assertTrue(file.length() > 0, "The file should not be empty.");
        assertEquals("Export process reached error threshold page request count: 11", ex.getMessage());
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
