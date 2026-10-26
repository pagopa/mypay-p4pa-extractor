package it.gov.pagopa.mypay2pu.extractor.service.xml;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.mypay2pu.extractor.service.files.XMLMarshallerService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FlussoRiversamentoXmlMarshallerService {

  private static final String XSD_PATH = "xsd/FlussoRiversamento.xsd";

  private final XMLMarshallerService xmlMarshallerService;
  private final JAXBContext jaxbContext;
  private final Schema schema;

  public FlussoRiversamentoXmlMarshallerService(XMLMarshallerService xmlMarshallerService) {
    this.xmlMarshallerService = xmlMarshallerService;
    try {
      this.jaxbContext = JAXBContext.newInstance(FlussoRiversamento.class);
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      try (InputStream xsd = new ClassPathResource(XSD_PATH).getInputStream()) {
        this.schema = factory.newSchema(new StreamSource(xsd));
      }
    } catch (JAXBException exception) {
      throw new IllegalStateException("Cannot initialize payments reporting JAXB context", exception);
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot initialize payments reporting XML schema", exception);
    }
  }

  public void marshal(FlussoRiversamento flow, Path outputPath) {
    String xml = xmlMarshallerService.marshal(FlussoRiversamento.class, flow, jaxbContext);
    validate(xml);
    try {
      Files.createDirectories(outputPath.getParent());
      Files.writeString(outputPath, xml);
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting XML " + outputPath, exception);
    }
  }

  private void validate(String xml) {
    try {
      schema.newValidator().validate(new StreamSource(new StringReader(xml)));
    } catch (Exception exception) {
      throw new IllegalStateException("Payments reporting XML does not conform to the XSD", exception);
    }
  }
}
