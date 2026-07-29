package it.gov.pagopa.mypay2pu.extractor.connector.mydictionary;

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
                            @Value("${rest.mydictionary.base-url}") String baseUrl) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
  }

  public String getSpontaneousFormStructure(String codXsdCausale) {
    String url = UriComponentsBuilder.fromUriString(baseUrl)
      .path("/get.html")
      .queryParam("codice", codXsdCausale)
      .toUriString();
    return restTemplate.getForObject(url, String.class);
  }
}
