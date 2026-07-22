package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "template.mail-esito-pagamento")
public record TemplateMailEsitoPagamentoProperties(
  String body,
  String subject
) {
}
