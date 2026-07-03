package it.gov.pagopa.mypay2pu.extractor.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cipher")
@Validated
public record CipherProperties(
  boolean fileEncryptEnabled,
  @NotBlank String fileEncryptPsw
) {
}
