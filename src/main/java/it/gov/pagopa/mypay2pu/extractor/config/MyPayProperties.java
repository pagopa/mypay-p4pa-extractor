package it.gov.pagopa.mypay2pu.extractor.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "mypay")
@Validated
public record MyPayProperties(
  @NestedConfigurationProperty @NotNull @Valid PathProperties path,
  @NestedConfigurationProperty @NotNull @Valid GlobalProperties globalProperties
) {

  public record PathProperties(@NotBlank String directoryRootEnti) {
  }

  public record GlobalProperties(
    @Name("notificaIo.subject") String ioTemplateSubject,
    @Name("notificaIo.markdown") String ioTemplateMessage
  ) {
  }
}
