package it.gov.pagopa.mypay2pu.extractor.service.xml;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.mypay2pu.extractor.service.files.XMLMarshallerService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FlussoRiversamentoXmlMarshallerService {

  private final XMLMarshallerService xmlMarshallerService;
  private final JAXBContext jaxbContext;

  public FlussoRiversamentoXmlMarshallerService(XMLMarshallerService xmlMarshallerService) {
    this.xmlMarshallerService = xmlMarshallerService;
    try {
      this.jaxbContext = JAXBContext.newInstance(FlussoRiversamento.class);
    } catch (JAXBException exception) {
      throw new IllegalStateException("Cannot initialize payments reporting JAXB context", exception);
    }
  }

  public void marshal(FlussoRiversamento flow, Path outputPath) {
    String xml = xmlMarshallerService.marshal(FlussoRiversamento.class, flow, jaxbContext);
    try {
      Files.createDirectories(outputPath.getParent());
      Files.writeString(outputPath, xml);
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot create payments reporting XML " + outputPath, exception);
    }
  }
}
