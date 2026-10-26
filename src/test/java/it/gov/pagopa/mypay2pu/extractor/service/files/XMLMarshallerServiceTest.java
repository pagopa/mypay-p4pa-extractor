package it.gov.pagopa.mypay2pu.extractor.service.files;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XMLMarshallerServiceTest {

  @Mock
  private JAXBContext jaxbContextMock;

  private final XMLMarshallerService service = new XMLMarshallerService();

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(jaxbContextMock);
  }

  @Test
  void givenJaxbAnnotatedValueWhenMarshalThenSerializeXml() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(FlussoRiversamento.class);

    String result = service.marshal(FlussoRiversamento.class, new FlussoRiversamento(), jaxbContext);

    assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
    assertTrue(result.contains("<FlussoRiversamento xmlns=\"http://www.digitpa.gov.it/schemas/2011/Pagamenti/\""));
  }

  @Test
  void givenJaxbFailureWhenMarshalThenThrowIllegalStateException() throws JAXBException {
    when(jaxbContextMock.createMarshaller()).thenThrow(new JAXBException("JAXB failure"));

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> service.marshal(FlussoRiversamento.class, new FlussoRiversamento(), jaxbContextMock)
    );

    assertTrue(exception.getMessage().contains(FlussoRiversamento.class.getName()));
    verify(jaxbContextMock).createMarshaller();
  }
}
