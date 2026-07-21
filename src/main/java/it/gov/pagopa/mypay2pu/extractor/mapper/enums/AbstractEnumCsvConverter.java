package it.gov.pagopa.mypay2pu.extractor.mapper.enums;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reusable base class to translate a raw DB value into the CSV value expected by the
 * destination migration system, backed by an enum (typically OpenAPI-generated and not
 * meant to be edited manually).
 * <p>
 * The decoding table is supplied once by each concrete converter and can optionally be
 * complemented by a default resolver for values that already match the destination enum.
 * Unrecognized DB values are reported as row-mapping failures.
 *
 * @param <E> the destination enum type
 */
public abstract class AbstractEnumCsvConverter<E extends Enum<E>> {

  private final Map<String, E> mappings;
  private final Function<String, E> defaultResolver;
  private final Class<E> enumType;
  private final String csvFieldName;

  protected AbstractEnumCsvConverter(Map<String, E> mappings, Class<E> enumType, String csvFieldName) {
    this(mappings, null, enumType, csvFieldName);
  }

  protected AbstractEnumCsvConverter(
    Map<String, E> mappings,
    Function<String, E> defaultResolver,
    Class<E> enumType,
    String csvFieldName
  ) {
    this.mappings = Map.copyOf(Objects.requireNonNull(mappings, "Mappings are required"));
    this.defaultResolver = defaultResolver;
    this.enumType = Objects.requireNonNull(enumType, "Enum type is required");
    this.csvFieldName = Objects.requireNonNull(csvFieldName, "CSV field name is required");
  }

  public E toCsvValue(String dbValue) {
    if (dbValue == null) {
      return null;
    }

    E enumValue = mappings.get(dbValue);
    if (enumValue == null) {
      enumValue = resolveDefault(dbValue);
    }
    return enumValue;
  }

  private E resolveDefault(String dbValue) {
    if (defaultResolver == null) {
      throw mappingException(dbValue, null);
    }

    try {
      E resolved = defaultResolver.apply(dbValue);
      if (resolved == null) {
        throw mappingException(dbValue, null);
      }
      return resolved;
    } catch (CsvRowMappingException e) {
      throw e;
    } catch (RuntimeException e) {
      throw mappingException(dbValue, e);
    }
  }

  private CsvRowMappingException mappingException(String dbValue, Throwable cause) {
    return new CsvRowMappingException(
      "EnumMapping",
      csvFieldName,
      dbValue,
      "Unrecognized value '" + dbValue + "' for enum " + enumType.getSimpleName(),
      cause
    );
  }
}
