package it.gov.pagopa.mypay2pu.extractor.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "extractor.export")
@Validated
public record ExtractorExportProperties(
  @NotBlank String storagePath,
  @Positive long multipartMaxFileSize,
  @Positive int exportPageSize,
  @Positive int avgRowSizeOrganizations,
  String brokerCf
) {
}
