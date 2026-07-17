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
    contextRunner
      .withPropertyValues(validPropertyValues())
      .run(context -> {
        assertNull(context.getStartupFailure());

        ExtractorExportProperties properties = context.getBean(ExtractorExportProperties.class);

        assertEquals("/tmp/extractions", properties.storagePath());
        assertEquals("/tmp", properties.tempBaseDir());
        assertEquals("12345678901", properties.brokerCf());
        assertEquals("IPA_CODE", properties.brokerIpaCode());
        assertEquals(1000, properties.resolveFileTypeConfiguration(MigrationFileType.ORGANIZATIONS).exportPageSize());
      });
  }

  @Test
  void whenOrganizationPageSizeIsInvalidThenStartupFailsValidation() {
    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=/tmp/extractions",
        "extractor.export.temp-base-dir=/tmp",
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
  void whenTypeConfigurationIsMissingThenStartupFailsValidation() {
    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=/tmp/extractions",
        "extractor.export.temp-base-dir=/tmp",
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
    contextRunner
      .withPropertyValues(
        "extractor.export.storage-path=/tmp/extractions",
        "extractor.export.temp-base-dir=/tmp",
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
    ExtractorExportProperties properties = new ExtractorExportProperties(
      "/tmp/extractions",
      "/tmp",
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

  private String[] validPropertyValues() {
    return new String[]{
      "extractor.export.storage-path=/tmp/extractions",
      "extractor.export.temp-base-dir=/tmp",
      "extractor.export.broker-cf=12345678901",
      "extractor.export.broker-ipa-code=IPA_CODE",
      "extractor.export.file-type-configurations.ORGANIZATIONS.export-page-size=1000"
    };
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(ExtractorExportProperties.class)
  static class TestConfiguration {
  }
}
