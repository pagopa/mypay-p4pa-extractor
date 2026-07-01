package it.gov.pagopa.mypay2pu.extractor.service;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.CsvBindByName;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvService {

  private void generateCsv(Path filePath, String[] header, List<String[]> rows) throws IOException {
    Files.createDirectories(filePath.getParent());
    try (Writer fileWriter = Files.newBufferedWriter(filePath);
         CSVWriter writer = new CSVWriter(
           fileWriter,
           ';',
           ICSVWriter.NO_QUOTE_CHARACTER,
           ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
           System.lineSeparator())) {
      writer.writeNext(header);
      rows.forEach(writer::writeNext);
    }
  }

  public <T> void generateCsvFromBeans(Path filePath, Class<T> beanType, String[] header, List<T> rows) throws IOException {
    Map<String, Method> gettersByColumn = resolveCsvGetters(beanType);
    List<String[]> csvRows = new ArrayList<>(rows.size());
    for (T rowBean : rows) {
      csvRows.add(toCsvRow(rowBean, header, gettersByColumn));
    }
    generateCsv(filePath, header, csvRows);
  }

  private <T> Map<String, Method> resolveCsvGetters(Class<T> beanType) throws IOException {
    Map<String, Method> gettersByColumn = new HashMap<>();
    Class<?> current = beanType;
    while (current != null && current != Object.class) {
      for (Field field : current.getDeclaredFields()) {
        CsvBindByName csvBindByName = field.getAnnotation(CsvBindByName.class);
        if (csvBindByName != null) {
          String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
          try {
            Method getter = beanType.getMethod(getterName);
            gettersByColumn.put(csvBindByName.column(), getter);
          } catch (NoSuchMethodException e) {
            throw new IOException("Missing getter for annotated field: " + field.getName(), e);
          }
        }
      }
      current = current.getSuperclass();
    }
    return gettersByColumn;
  }

  private String[] toCsvRow(Object bean, String[] header, Map<String, Method> gettersByColumn) throws IOException {
    String[] row = new String[header.length];
    for (int i = 0; i < header.length; i++) {
      Method getter = gettersByColumn.get(header[i]);
      if (getter == null) {
        throw new IOException("Missing @CsvBindByName mapping for column: " + header[i]);
      }
      try {
        Object value = getter.invoke(bean);
        row[i] = value != null ? value.toString() : "";
      } catch (ReflectiveOperationException e) {
        throw new IOException("Cannot read value for column: " + header[i], e);
      }
    }
    return row;
  }
}
