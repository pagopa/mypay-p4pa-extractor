package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties(prefix = "mypay.global-properties")
public record MyPayProperties(
  @Name("notificaIo.subject") String ioTemplateSubject,
  @Name("notificaIo.markdown") String ioTemplateMessage
) {
}
