package it.gov.pagopa.mypay2pu.extractor.mapper.enums;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractEnumConverter<E extends Enum<E>> {

  private final Map<String, E> mappings;
  private final Function<String, E> defaultResolver;

  protected AbstractEnumConverter(Map<String, E> mappings, Function<String, E> defaultResolver) {
    this.mappings = Map.copyOf(Objects.requireNonNull(mappings, "Mappings are required"));
    this.defaultResolver = defaultResolver;
  }

  protected E convert(String sourceValue) {
    if (sourceValue == null) {
      return null;
    }

    E enumValue = mappings.get(sourceValue);
    return enumValue != null || defaultResolver == null
      ? enumValue
      : defaultResolver.apply(sourceValue);
  }
}
