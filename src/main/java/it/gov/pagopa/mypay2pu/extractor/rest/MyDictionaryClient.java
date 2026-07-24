package it.gov.pagopa.mypay2pu.extractor.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MyDictionaryClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public MyDictionaryClient(RestTemplateBuilder restTemplateBuilder,
                            @Value("${mydictionary.base-url:}") String baseUrl) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
  }

  public String getSpontaneousFormStructure(String debtPositionTypeCode) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException("mydictionary.base-url must be configured");
    }
    String url = UriComponentsBuilder.fromUriString(baseUrl)
      .path("/get.html")
      .queryParam("codice", debtPositionTypeCode)
      .toUriString();
    return restTemplate.getForObject(url, String.class);
  }
}
