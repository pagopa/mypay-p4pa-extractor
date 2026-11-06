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
public abstract class AbstractEnumCsvConverter<E extends Enum<E>> extends AbstractEnumConverter<E> {

  private final Class<E> enumType;

  protected AbstractEnumCsvConverter(Map<String, E> mappings, Class<E> enumType, String csvFieldName) {
    this(mappings, null, enumType, csvFieldName);
  }

  protected AbstractEnumCsvConverter(
    Map<String, E> mappings,
    Function<String, E> defaultResolver,
    Class<E> enumType,
    String csvFieldName
  ) {
    super(mappings, defaultResolver, csvFieldName);
    this.enumType = Objects.requireNonNull(enumType, "Enum type is required");
  }

  public E toCsvValue(String dbValue) {
    if (dbValue == null) {
      return null;
    }

    try {
      E resolved = convert(dbValue);
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
      getFieldName(),
      dbValue,
      "Unrecognized value '" + dbValue + "' for enum " + enumType.getSimpleName(),
      cause
    );
  }
}
