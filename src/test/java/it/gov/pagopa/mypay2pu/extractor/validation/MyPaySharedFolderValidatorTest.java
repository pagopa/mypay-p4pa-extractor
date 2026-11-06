package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyPaySharedFolderValidatorTest {

  @TempDir
  Path tempDir;

  @Test
  void givenAvailableDirectoryWhenValidateThenSucceed() {
    MyPaySharedFolderValidator validator = validator(tempDir);

    assertDoesNotThrow(validator::validateMyPaySharedFolder);
  }

  @Test
  void givenUnavailableDirectoryWhenValidateThenThrowBadRequest() {
    Path missingDirectory = tempDir.resolve("missing");
    MyPaySharedFolderValidator validator = validator(missingDirectory);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      validator::validateMyPaySharedFolder
    );

    assertEquals("MYPAY_SHARED_FOLDER_UNAVAILABLE", exception.getCode());
    assertEquals("MyPay shared folder is unavailable", exception.getMessage());
  }

  private MyPaySharedFolderValidator validator(Path path) {
    return new MyPaySharedFolderValidator(
      new MyPayProperties(
        new MyPayProperties.PathProperties(path.toString()),
        new MyPayProperties.GlobalProperties(null, null)
      )
    );
  }
}
