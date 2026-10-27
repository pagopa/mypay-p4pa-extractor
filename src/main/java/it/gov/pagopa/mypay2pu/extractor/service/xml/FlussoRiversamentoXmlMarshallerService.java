package it.gov.pagopa.mypay2pu.extractor.service.xml;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.mypay2pu.extractor.service.files.XMLMarshallerService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

@Service
public class FlussoRiversamentoXmlMarshallerService {

  private final XMLMarshallerService xmlMarshallerService;
  private final JAXBContext jaxbContext;
  private final Schema schema;

  public FlussoRiversamentoXmlMarshallerService(XMLMarshallerService xmlMarshallerService) {
    this.xmlMarshallerService = xmlMarshallerService;
    try {
      this.jaxbContext = JAXBContext.newInstance(FlussoRiversamento.class);
    } catch (JAXBException exception) {
      throw new IllegalStateException("Cannot initialize payments reporting JAXB context", exception);
    }
    this.schema = loadSchema();
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

  private Schema loadSchema() {
    URL schemaUrl = getClass().getResource("/xsd/FlussoRiversamento.xsd");
    if (schemaUrl == null) {
      throw new IllegalStateException("Cannot find payments reporting XSD");
    }
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      return schemaFactory.newSchema(schemaUrl);
    } catch (SAXException exception) {
      throw new IllegalStateException("Cannot initialize payments reporting XSD validation", exception);
    }
  }

  private void validate(String xml) {
    try {
      schema.newValidator().validate(new StreamSource(new StringReader(xml)));
    } catch (SAXException | IOException exception) {
      throw new IllegalStateException("Payments reporting XML does not conform to the XSD", exception);
    }
  }
}
