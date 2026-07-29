package it.gov.pagopa.mypay2pu.extractor.connector.mydictionary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyDictionaryClientTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;
  @Mock
  private RestTemplate restTemplateMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(restTemplateBuilderMock, restTemplateMock);
  }

  @Test
  void givenConfiguredBaseUrlWhenGetSpontaneousFormStructureThenInvokeExpectedUrl() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    String expectedResponse = "{\"key\":\"value\"}";
    String expectedUrl = "https://mydictionary.test/get.html?codice=TAX_CODE";
    when(restTemplateMock.getForObject(expectedUrl, String.class)).thenReturn(expectedResponse);

    MyDictionaryClient client = new MyDictionaryClient(restTemplateBuilderMock, "https://mydictionary.test");
    String result = client.getSpontaneousFormStructure("TAX_CODE");

    assertEquals(expectedResponse, result);
    verify(restTemplateBuilderMock).build();
    verify(restTemplateMock).getForObject(expectedUrl, String.class);
  }
}
