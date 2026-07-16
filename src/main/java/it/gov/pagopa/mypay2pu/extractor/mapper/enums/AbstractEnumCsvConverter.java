package it.gov.pagopa.mypay2pu.extractor.mapper.enums;

import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;

import java.util.Map;
import java.util.function.Function;

/**
 * Reusable base class to translate a raw DB value into the CSV value expected by the
 * destination migration system, backed by an enum (typically OpenAPI-generated and not
 * meant to be edited manually).
 * <p>
 * Two resolution strategies are supported, and can be mixed on the same converter:
 * <ul>
 *   <li>an explicit decode table ({@link #explicitMappings()}), for DB codes that do not
 *   match the enum's own value (e.g. {@code "A"} -&gt; {@code ACTIVE});</li>
 *   <li>a default resolver ({@link #defaultResolver()}), for DB values that already match
 *   the enum's value 1:1 (typically the enum's own {@code fromValue(String)} factory method).</li>
 * </ul>
 * Unrecognized DB values are reported as recoverable row-mapping failures.
 *
 * @param <E> the destination enum type
 */
public abstract class AbstractEnumCsvConverter<E extends Enum<E>> {

  public String toCsvValue(String dbValue) {
    if (dbValue == null) {
      return null;
    }

    E enumValue = explicitMappings().get(dbValue);
    if (enumValue == null) {
      enumValue = resolveDefault(dbValue);
    }

    return csvValueExtractor().apply(enumValue);
  }

  private E resolveDefault(String dbValue) {
    E resolved;
    try {
      resolved = defaultResolver().apply(dbValue);
    } catch (RuntimeException e) {
      throw mappingException(dbValue, e);
    }

    if (resolved == null) {
      throw mappingException(dbValue, null);
    }
    return resolved;
  }

  private CsvRowMappingException mappingException(String dbValue, Throwable cause) {
    return new CsvRowMappingException(
      csvFieldName(),
      dbValue,
      "Unrecognized value '" + dbValue + "' for enum " + enumType().getSimpleName(),
      cause
    );
  }

  protected Map<String, E> explicitMappings() {
    return Map.of();
  }

  protected abstract Function<String, E> defaultResolver();

  protected abstract Function<E, String> csvValueExtractor();

  protected abstract Class<E> enumType();

  /**
   * CSV field that receives this enum value and is reported when conversion fails.
   */
  protected abstract String csvFieldName();
}