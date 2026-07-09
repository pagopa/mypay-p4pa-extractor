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
    String location = "queries/test.sql";
    String sqlWithSpaces = "  SELECT * FROM table_name  ";

    when(resourceLoader.getResource("classpath:db/queries/test.sql")).thenReturn(resource);
    when(resource.exists()).thenReturn(true);
    when(resource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(sqlWithSpaces.getBytes(StandardCharsets.UTF_8)));

    String firstLoad = sqlLoader.load(location);
    String secondLoad = sqlLoader.load(location);

    assertEquals("SELECT * FROM table_name", firstLoad);
    assertSame(firstLoad, secondLoad);
    verify(resourceLoader, times(1)).getResource("classpath:db/queries/test.sql");
    verify(resource, times(1)).getInputStream();
  }

  @Test
  void givenMissingSqlResourceWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);
    String location = "queries/missing.sql";

    when(resourceLoader.getResource("classpath:db/queries/missing.sql")).thenReturn(resource);
    when(resource.exists()).thenReturn(false);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> sqlLoader.load(location));

    assertEquals("SQL resource not found: db/queries/missing.sql", exception.getMessage());
  }

  @Test
  void givenPathTraversalWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> sqlLoader.load("../outside.sql"));

    assertEquals("SQL resource location escapes base path: ../outside.sql", exception.getMessage());
  }

  @Test
  void givenUnreadableSqlResourceWhenLoadThenThrowUncheckedIOException() throws IOException {
    SqlLoader sqlLoader = new SqlLoader(resourceLoader);
    String location = "queries/broken.sql";
    IOException ioException = new IOException("read failure");

    when(resourceLoader.getResource("classpath:db/queries/broken.sql")).thenReturn(resource);
    when(resource.exists()).thenReturn(true);
    when(resource.getInputStream()).thenThrow(ioException);

    UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> sqlLoader.load(location));

    assertEquals("Cannot read SQL resource: db/queries/broken.sql", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
    assertEquals("read failure", exception.getCause().getMessage());
  }
}
