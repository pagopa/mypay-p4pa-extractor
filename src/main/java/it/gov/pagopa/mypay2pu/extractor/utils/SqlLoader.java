package it.gov.pagopa.mypay2pu.extractor.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SqlLoader {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private final ResourceLoader resourceLoader;
  private final Map<String, String> sqlCache = new ConcurrentHashMap<>();

  public SqlLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String load(String location) {
    return sqlCache.computeIfAbsent(location, this::readSql);
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
