package it.gov.pagopa.mypay2pu.extractor.service;

import com.opencsv.bean.CsvBindByName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvServiceTest {

  private final CsvService csvService = new CsvService();

  @Test
  void generateCsvFromBeans_shouldWriteHeaderAndRows() throws IOException {
    Path filePath = Path.of("build", "tmp", "test", "csv-service", "organizations.csv");
    String[] header = new String[]{"Code", "Name"};
    List<TestBean> rows = List.of(
      new TestBean("001", "Comune A"),
      new TestBean("002", "Comune B")
    );

    csvService.generateCsvFromBeans(filePath, TestBean.class, header, rows);

    assertTrue(Files.exists(filePath));
    List<String> lines = Files.readAllLines(filePath);
    assertEquals(3, lines.size());
    assertEquals("Code;Name", lines.get(0));
    assertEquals("001;Comune A", lines.get(1));
    assertEquals("002;Comune B", lines.get(2));
  }

  @Test
  void generateCsvFromBeans_shouldWriteEmptyStringForNullValues() throws IOException {
    Path filePath = Path.of("build", "tmp", "test", "csv-service", "nulls.csv");
    String[] header = new String[]{"Code", "Name"};
    List<TestBean> rows = List.of(new TestBean("001", null));

    csvService.generateCsvFromBeans(filePath, TestBean.class, header, rows);

    List<String> lines = Files.readAllLines(filePath);
    assertEquals(2, lines.size());
    assertEquals("001;", lines.get(1));
  }

  @Test
  void generateCsvFromBeans_shouldFailWhenHeaderColumnIsNotMapped() {
    Path filePath = Path.of("build", "tmp", "test", "csv-service", "invalid-header.csv");
    String[] header = new String[]{"Code", "UnknownColumn"};
    List<TestBean> rows = List.of(new TestBean("001", "Comune A"));

    IOException exception = assertThrows(IOException.class, () ->
      csvService.generateCsvFromBeans(filePath, TestBean.class, header, rows)
    );

    assertEquals("Missing @CsvBindByName mapping for column: UnknownColumn", exception.getMessage());
  }

  @Test
  void generateCsvFromBeans_shouldFailWhenGetterIsMissing() {
    Path filePath = Path.of("build", "tmp", "test", "csv-service", "missing-getter.csv");
    String[] header = new String[]{"Code"};
    List<BeanWithoutGetter> rows = List.of(new BeanWithoutGetter("001"));

    IOException exception = assertThrows(IOException.class, () ->
      csvService.generateCsvFromBeans(filePath, BeanWithoutGetter.class, header, rows)
    );

    assertTrue(exception.getMessage().contains("Missing getter for annotated field: code"));
  }

  @Test
  void generateCsvFromBeans_shouldResolveAnnotatedFieldsFromSuperclass() throws IOException {
    Path filePath = Path.of("build", "tmp", "test", "csv-service", "inherited.csv");
    String[] header = new String[]{"Code"};
    List<ChildBean> rows = List.of(new ChildBean("009"));

    csvService.generateCsvFromBeans(filePath, ChildBean.class, header, rows);

    List<String> lines = Files.readAllLines(filePath);
    assertEquals(2, lines.size());
    assertEquals("009", lines.get(1));
  }

  private static class TestBean {
    @CsvBindByName(column = "Code")
    private final String code;
    @CsvBindByName(column = "Name")
    private final String name;

    private TestBean(String code, String name) {
      this.code = code;
      this.name = name;
    }

    public String getCode() {
      return code;
    }

    public String getName() {
      return name;
    }
  }

  private static class BeanWithoutGetter {
    @CsvBindByName(column = "Code")
    private final String code;

    private BeanWithoutGetter(String code) {
      this.code = code;
    }
  }

  private static class ParentBean {
    @CsvBindByName(column = "Code")
    private final String code;

    private ParentBean(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  private static class ChildBean extends ParentBean {
    private ChildBean(String code) {
      super(code);
    }
  }
}
