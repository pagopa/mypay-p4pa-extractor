package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mypivot")
public record MyPivotProperties(boolean enabled) {
}
