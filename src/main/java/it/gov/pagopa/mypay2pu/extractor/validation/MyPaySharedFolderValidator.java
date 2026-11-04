package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
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

  public Path sharedFolderPath() {
    return sharedFolderPath;
  }

  public String unavailableDirectoryMessage() {
    return "Property mypay.path.directory-root-enti must point to an existing directory: " + sharedFolderPath;
  }
}
