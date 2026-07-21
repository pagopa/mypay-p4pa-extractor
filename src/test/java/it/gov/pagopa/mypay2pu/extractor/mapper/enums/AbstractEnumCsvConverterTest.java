package it.gov.pagopa.mypay2pu.extractor.mapper.enums;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractEnumCsvConverterTest {

  @Test
  void toCsvValueShouldReturnMappedEnum() {
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of("db-active", TestEnum.ACTIVE));

    assertEquals(TestEnum.ACTIVE, converter.toCsvValue("db-active"));
  }

  @Test
  void toCsvValueShouldUseResolverWhenDirectEnumValueIsProvided() {
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of("db-draft", TestEnum.DRAFT), TestEnum::valueOf);

    assertEquals(TestEnum.ACTIVE, converter.toCsvValue("ACTIVE"));
  }

  @Test
  void toCsvValueShouldReturnNullWhenSourceValueIsNull() {
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of("db-active", TestEnum.ACTIVE));

    assertNull(converter.toCsvValue(null));
  }

  @Test
  void toCsvValueShouldThrowWhenValueIsUnmappedAndNoResolverExists() {
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of("db-active", TestEnum.ACTIVE));

    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> converter.toCsvValue("UNKNOWN")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("testField", exception.getField());
    assertEquals("UNKNOWN", exception.getRejectedValue());
  }

  @Test
  void toCsvValueShouldThrowWhenResolverReturnsNull() {
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of(), value -> null);

    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> converter.toCsvValue("UNKNOWN")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("testField", exception.getField());
    assertEquals("UNKNOWN", exception.getRejectedValue());
  }

  @Test
  void toCsvValueShouldWrapResolverRuntimeException() {
    RuntimeException cause = new IllegalStateException("boom");
    TestEnumCsvConverter converter = new TestEnumCsvConverter(Map.of(), value -> {
      throw cause;
    });

    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> converter.toCsvValue("UNKNOWN")
    );

    assertEquals("EnumMapping", exception.getErrorCode());
    assertEquals("testField", exception.getField());
    assertEquals("UNKNOWN", exception.getRejectedValue());
    assertSame(cause, exception.getCause());
  }

  private enum TestEnum {
    ACTIVE,
    DRAFT
  }

  private static final class TestEnumCsvConverter extends AbstractEnumCsvConverter<TestEnum> {

    private TestEnumCsvConverter(Map<String, TestEnum> mappings) {
      super(mappings, TestEnum.class, "testField");
    }

    private TestEnumCsvConverter(Map<String, TestEnum> mappings, Function<String, TestEnum> defaultResolver) {
      super(mappings, defaultResolver, TestEnum.class, "testField");
    }
  }
}
