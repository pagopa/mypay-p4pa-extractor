package it.gov.pagopa.mypay2pu.extractor.config;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix = "extractor.export")
@Validated
public record ExtractorExportProperties(
  @NotBlank String storagePath,
  @Positive long multipartMaxFileSize,
  @Positive int exportPageSize,
  @NotBlank String brokerCf,
  String brokerIpaCode,
  @NotEmpty Map<MigrationFileType, @Valid FileTypeConfiguration> fileTypeConfigurations
) {

  public ExtractorExportProperties {
    brokerIpaCode = StringUtils.hasText(brokerIpaCode) ? brokerIpaCode : null;
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

  public record FileTypeConfiguration(
    @Positive int avgRowSize
  ) {
  }
}
