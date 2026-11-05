package it.gov.pagopa.mypay2pu.extractor.mapper.enums;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractEnumConverter<E extends Enum<E>> {

  private final Map<String, E> mappings;
  private final Function<String, E> defaultResolver;
  private final String fieldName;

  protected AbstractEnumConverter(
    Map<String, E> mappings,
    Function<String, E> defaultResolver,
    String fieldName
  ) {
    this.mappings = Map.copyOf(Objects.requireNonNull(mappings, "Mappings are required"));
    this.defaultResolver = defaultResolver;
    this.fieldName = Objects.requireNonNull(fieldName, "Field name is required");
  }

  protected E convert(String sourceValue) {
    if (sourceValue == null) {
      return null;
    }

    E enumValue = mappings.get(sourceValue);
    if (enumValue != null || defaultResolver == null) {
      return enumValue;
    }

    try {
      return defaultResolver.apply(sourceValue);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(
        "Unrecognized enum value '%s' for field '%s'".formatted(sourceValue, fieldName),
        exception
      );
    }
  }

  protected String getFieldName() {
    return fieldName;
  }
}
