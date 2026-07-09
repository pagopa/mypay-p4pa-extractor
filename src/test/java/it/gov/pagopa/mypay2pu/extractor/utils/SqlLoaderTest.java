package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlLoaderTest {

  @Test
  void givenValidSqlResourceWhenLoadTwiceThenTrimAndCacheResult() {
    SqlLoader sqlLoader = new SqlLoader();
    String location = "mypay/organization/organization.sql";

    String firstLoad = sqlLoader.load(location);
    String secondLoad = sqlLoader.load(location);

    assertEquals(firstLoad.trim(), firstLoad);
    assertSame(firstLoad, secondLoad);
  }

  @Test
  void givenMissingSqlResourceWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader();
    String location = "mypay/organization/missing.sql";

    assertThrows(IllegalArgumentException.class,
      () -> sqlLoader.load(location),
      "SQL resource not found");
  }

  @Test
  void givenPathTraversalWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader();

    assertThrows(IllegalArgumentException.class,
      () -> sqlLoader.load("../outside.sql"),
        "SQL resource location escapes base path");

  }

  @Test
  void givenBlankLocationWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader();

    assertThrows(IllegalArgumentException.class,
      () -> sqlLoader.load(" "),
      "SQL resource location must not be blank");
  }

  @Test
  void givenAbsoluteLocationWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader();
    String absoluteLocation = Path.of("").toAbsolutePath().resolve("absolute.sql").toString();

    assertThrows(IllegalArgumentException.class,
      () -> sqlLoader.load(absoluteLocation),
      "SQL resource location must be relative");

  }

  @Test
  void givenInvalidLocationWhenLoadThenThrowIllegalArgumentException() {
    SqlLoader sqlLoader = new SqlLoader();

    assertThrows(IllegalArgumentException.class,
      () -> sqlLoader.load("mypay/organization/\0invalid.sql"),
      "Invalid SQL resource location");
  }

  @Test
  void givenUnreadableSqlResourceWhenLoadThenThrowUncheckedIOException() {
    SqlLoader sqlLoader = new SqlLoader() {
      @Override
      ClassPathResource getResource(String location) {
        return new ClassPathResource(location) {
          @Override
          public boolean exists() {
            return true;
          }

          @Override
          public InputStream getInputStream() throws IOException {
            throw new IOException("read failure");
          }
        };
      }
    };
    String location = "mypay/organization/broken.sql";

    UncheckedIOException exception = assertThrows(UncheckedIOException.class,
      () -> sqlLoader.load(location),
      "Cannot read SQL resource");

    assertInstanceOf(IOException.class, exception.getCause());
    assertEquals("read failure", exception.getCause().getMessage());
  }
}
