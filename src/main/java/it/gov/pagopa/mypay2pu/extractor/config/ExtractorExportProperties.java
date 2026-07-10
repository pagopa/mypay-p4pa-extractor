package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "extractor.export")
@Validated
public record ExtractorExportProperties(
  String brokerCf
) {
}
