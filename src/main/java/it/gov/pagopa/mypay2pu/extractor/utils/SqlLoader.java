package it.gov.pagopa.mypay2pu.extractor.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SqlLoader {

  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final Path BASE_SQL_PATH = Path.of("db").normalize();

  private final ResourceLoader resourceLoader;
  private final Map<String, String> sqlCache = new ConcurrentHashMap<>();

  public SqlLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String load(String location) {
    String normalizedLocation = normalizeLocation(location);
    return sqlCache.computeIfAbsent(normalizedLocation, this::readSql);
  }

  private String normalizeLocation(String location) {
    if (location == null || location.isBlank()) {
      throw new IllegalArgumentException("SQL resource location must not be blank");
    }

    Path inputPath;
    try {
      inputPath = Path.of(location).normalize();
    } catch (InvalidPathException exception) {
      throw new IllegalArgumentException("Invalid SQL resource location: " + location, exception);
    }

    if (inputPath.isAbsolute()) {
      throw new IllegalArgumentException("SQL resource location must be relative: " + location);
    }

    Path normalizedPath = inputPath.startsWith(BASE_SQL_PATH)
      ? inputPath
      : BASE_SQL_PATH.resolve(inputPath).normalize();

    if (!normalizedPath.startsWith(BASE_SQL_PATH)) {
      throw new IllegalArgumentException("SQL resource location escapes base path: " + location);
    }

    return normalizedPath.toString().replace('\\', '/');
  }

  private String readSql(String location) {
    Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + location);
    if (!resource.exists()) {
      throw new IllegalArgumentException("SQL resource not found: " + location);
    }

    try (InputStream inputStream = resource.getInputStream()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot read SQL resource: " + location, exception);
    }
  }
}
