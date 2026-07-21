package it.gov.pagopa.mypay2pu.extractor.config;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtractorExportPropertiesTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(
      ConfigurationPropertiesAutoConfiguration.class,
      ValidationAutoConfiguration.class
    ))
    .withUserConfiguration(TestConfiguration.class);

  @Test
  void whenPropertiesAreValidThenBindAndResolveOrganizationsConfig() {
    ExistingDirectories existingDirectories = createExistingDirectories();
    contextRunner
      .withPropertyValues(validPropertyValues(existingDirectories))
      .run(context -> {
        assertNull(context.getStartupFailure());

        ExtractorExportProperties properties = context.getBean(ExtractorExportProperties.class);

        assertEquals(existingDirectories.storagePath().toString(), properties.storagePath());
        assertEquals(existingDirectories.tempBaseDir().toString(), properties.tempBaseDir());
        assertEquals("12345678901", properties.brokerCf());
        assertEquals("IPA_CODE", properties.brokerIpaCode());
        assertEquals(1000, properties.resolveFileTypeConfiguration(MigrationFileType.ORGANIZATIONS).exportPageSize());
      });
  }

  @Test
  void whenTypeConfigurationIsMissingThenStartupFailsValidation() {
    ExistingDirectories existingDirectories = createExistingDirectories();
    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=" + existingDirectories.storagePath(),
        "extractor.export.temp-base-dir=" + existingDirectories.tempBaseDir(),
        "extractor.export.broker-cf=12345678901",
        "extractor.export.broker-ipa-code=IPA_CODE"
      )
      .run(context -> {
        Throwable startupFailure = context.getStartupFailure();

        assertNotNull(startupFailure);
        assertEquals(ConfigurationPropertiesBindException.class, startupFailure.getClass());
      });
  }

  @Test
  void whenTypeConfigurationIsInvalidThenStartupFailsValidation() {
    ExistingDirectories existingDirectories = createExistingDirectories();
    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=" + existingDirectories.storagePath(),
        "extractor.export.temp-base-dir=" + existingDirectories.tempBaseDir(),
        "extractor.export.broker-cf=12345678901",
        "extractor.export.broker-ipa-code=IPA_CODE",
        "extractor.export.file-type-configurations.ORGANIZATIONS.export-page-size=0"
      )
      .run(context -> {
        Throwable startupFailure = context.getStartupFailure();

        assertNotNull(startupFailure);
        assertEquals(ConfigurationPropertiesBindException.class, startupFailure.getClass());
      });
  }

  @Test
  void whenRequestedTypeIsNotConfiguredThenThrowClearError() {
    ExistingDirectories existingDirectories = createExistingDirectories();
    ExtractorExportProperties properties = new ExtractorExportProperties(
      existingDirectories.storagePath().toString(),
      existingDirectories.tempBaseDir().toString(),
      "12345678901",
      "IPA_CODE",
      Map.of(MigrationFileType.ORG_SIL_SERVICES, new ExtractorExportProperties.FileTypeConfiguration(500))
    );

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> properties.resolveFileTypeConfiguration(MigrationFileType.ORGANIZATIONS)
    );

    assertEquals(
      "Missing extractor.export.file-type-configurations entry for migration file type ORGANIZATIONS",
      exception.getMessage()
    );
  }

  @Test
  void whenStoragePathDoesNotExistThenStartupFails() {
    ExistingDirectories existingDirectories = createExistingDirectories();
    Path nonExistentStoragePath = existingDirectories.storagePath().resolve("missing-dir");

    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=" + nonExistentStoragePath,
        "extractor.export.temp-base-dir=" + existingDirectories.tempBaseDir(),
        "extractor.export.broker-cf=12345678901",
        "extractor.export.broker-ipa-code=IPA_CODE",
        "extractor.export.file-type-configurations.ORGANIZATIONS.export-page-size=1000"
      )
      .run(context -> {
        Throwable startupFailure = context.getStartupFailure();
        assertNotNull(startupFailure);
      });
  }

  private String[] validPropertyValues(ExistingDirectories existingDirectories) {
    return new String[]{
      "extractor.export.storage-path=" + existingDirectories.storagePath(),
      "extractor.export.temp-base-dir=" + existingDirectories.tempBaseDir(),
      "extractor.export.broker-cf=12345678901",
      "extractor.export.broker-ipa-code=IPA_CODE",
      "extractor.export.file-type-configurations.ORGANIZATIONS.export-page-size=1000"
    };
  }

  private ExistingDirectories createExistingDirectories() {
    try {
      Path storagePath = Files.createTempDirectory("extractor-export-storage-");
      Path tempBaseDir = Files.createTempDirectory("extractor-export-temp-");
      return new ExistingDirectories(storagePath, tempBaseDir);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create temporary directories for test setup", e);
    }
  }

  private record ExistingDirectories(Path storagePath, Path tempBaseDir) {
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(ExtractorExportProperties.class)
  static class TestConfiguration {
  }
}
