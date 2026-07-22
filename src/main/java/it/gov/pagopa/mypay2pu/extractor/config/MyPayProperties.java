package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "mypay")
public record MyPayProperties(
  Map<String, String> globalProperties
) {
}
