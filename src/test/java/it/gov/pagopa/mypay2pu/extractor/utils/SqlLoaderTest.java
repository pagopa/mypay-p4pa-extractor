package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlLoaderTest {

  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private Resource resource;

  @Test
  void givenValidSqlResourceWhenLoadTwiceThenTrimAndCacheResult() throws IOException {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);
    String location = "db/queries/test.sql";
    String sqlWithSpaces = "  SELECT * FROM table_name  ";

    when(resourceLoader.getResource("classpath:" + location)).thenReturn(resource);
    when(resource.exists()).thenReturn(true);
    when(resource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(sqlWithSpaces.getBytes(StandardCharsets.UTF_8)));

    String firstLoad = sqlLoader.load(location);
    String secondLoad = sqlLoader.load(location);

    assertEquals("SELECT * FROM table_name", firstLoad);
    assertSame(firstLoad, secondLoad);
    verify(resourceLoader, times(1)).getResource("classpath:" + location);
    verify(resource, times(1)).getInputStream();
  }

  @Test
  void givenMissingSqlResourceWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);
    String location = "db/queries/missing.sql";

    when(resourceLoader.getResource("classpath:" + location)).thenReturn(resource);
    when(resource.exists()).thenReturn(false);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> sqlLoader.load(location));

    assertEquals("SQL resource not found: " + location, exception.getMessage());
  }

  @Test
  void givenUnreadableSqlResourceWhenLoadThenThrowUncheckedIOException() throws IOException {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);
    String location = "db/queries/broken.sql";
    IOException ioException = new IOException("read failure");

    when(resourceLoader.getResource("classpath:" + location)).thenReturn(resource);
    when(resource.exists()).thenReturn(true);
    when(resource.getInputStream()).thenThrow(ioException);

    UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> sqlLoader.load(location));

    assertEquals("Cannot read SQL resource: " + location, exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
    assertEquals("read failure", exception.getCause().getMessage());
  }
}
