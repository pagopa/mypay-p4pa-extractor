package it.gov.pagopa.mypay2pu.extractor.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GsonConfig {

  @Bean
  public Gson gson() {
    return new Gson();
  }
}
