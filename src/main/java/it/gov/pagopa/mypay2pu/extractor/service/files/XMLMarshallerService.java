package it.gov.pagopa.mypay2pu.extractor.service.files;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class XMLMarshallerService {

  public <T> String marshal(Class<T> clazz, T value, JAXBContext jaxbContext) {
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      StringWriter stringWriter = new StringWriter();
      marshaller.marshal(value, stringWriter);
      return stringWriter.toString();
    } catch (JAXBException exception) {
      throw new IllegalStateException("Cannot marshal XML class " + clazz.getName(), exception);
    }
  }
}
