package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MyPaySharedFolderValidator {

  private final Path sharedFolderPath;

  public MyPaySharedFolderValidator(MyPayProperties myPayProperties) {
    this.sharedFolderPath = Path.of(myPayProperties.path().directoryRootEnti());
  }

  public boolean isDirectoryAvailable() {
    return Files.isDirectory(sharedFolderPath);
  }

  public void validateMyPaySharedFolder() {
    if (!isDirectoryAvailable()) {
      throw new BadRequestException("MYPAY_SHARED_FOLDER_UNAVAILABLE", "MyPay shared folder is unavailable");
    }
  }

  public Path getMyPaySharedFolderPath() {
    return sharedFolderPath;
  }

}
