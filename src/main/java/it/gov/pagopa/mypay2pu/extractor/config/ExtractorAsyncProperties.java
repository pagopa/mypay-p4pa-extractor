package it.gov.pagopa.mypay2pu.extractor.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "extractor.async")
@Validated
public record ExtractorAsyncProperties(
  @Positive int corePoolSize,
  @Positive int maxPoolSize,
  @Positive int queueCapacity
) {
}
