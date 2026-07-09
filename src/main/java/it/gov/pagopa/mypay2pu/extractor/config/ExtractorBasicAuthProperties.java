package it.gov.pagopa.mypay2pu.extractor.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "extractor.basic-auth")
@Validated
public record ExtractorBasicAuthProperties(
  @NotBlank String username,
  @NotBlank String password
) {
}
