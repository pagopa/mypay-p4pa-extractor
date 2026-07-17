package it.gov.pagopa.mypay2pu.extractor.config;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@ConfigurationProperties(prefix = "extractor.export")
@Validated
public record ExtractorExportProperties(
  @NotBlank String storagePath,
  @NotBlank String tempBaseDir,
  @NotBlank String brokerCf,
  @NotBlank String brokerIpaCode,
  @NestedConfigurationProperty
  @NotEmpty Map<MigrationFileType, @Valid FileTypeConfiguration> fileTypeConfigurations) {

  @PostConstruct
  public void validateDirectoriesExist() {
    validateExistingDirectory("extractor.export.storage-path", storagePath);
    validateExistingDirectory("extractor.export.temp-base-dir", tempBaseDir);
  }

  public FileTypeConfiguration resolveFileTypeConfiguration(MigrationFileType migrationFileType) {
    FileTypeConfiguration configuration = fileTypeConfigurations != null
      ? fileTypeConfigurations.get(migrationFileType)
      : null;
    if (configuration == null) {
      throw new IllegalStateException(
        "Missing extractor.export.file-type-configurations entry for migration file type " + migrationFileType
      );
    }
    return configuration;
  }

  private void validateExistingDirectory(String propertyName, String directoryPath) {
    Path path = Path.of(directoryPath);
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      throw new IllegalStateException("Property " + propertyName + " must point to an existing directory: " + directoryPath);
    }
  }

  public record FileTypeConfiguration(@Positive int exportPageSize) { }
}
