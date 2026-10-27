package it.gov.pagopa.mypay2pu.extractor.service.xml;

import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoliPagamenti;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivoco;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG;
import it.gov.digitpa.schemas._2011.pagamenti.CtIstitutoMittente;
import it.gov.digitpa.schemas._2011.pagamenti.CtIstitutoRicevente;
import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.gov.pagopa.mypay2pu.extractor.service.files.XMLMarshallerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.datatype.DatatypeFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FlussoRiversamentoXmlMarshallerServiceTest {

  @TempDir
  Path tempDir;

  private final FlussoRiversamentoXmlMarshallerService marshaller =
    new FlussoRiversamentoXmlMarshallerService(new XMLMarshallerService());

  @Test
  void givenValidFlowWhenMarshalThenWriteSchemaValidXml() throws Exception {
    Path xmlPath = tempDir.resolve("FLOW_1.xml");

    marshaller.marshal(flow(), xmlPath);

    String xml = Files.readString(xmlPath);
    assertTrue(xml.contains("<FlussoRiversamento xmlns=\"http://www.digitpa.gov.it/schemas/2011/Pagamenti/\""));
    assertTrue(xml.contains("<importoTotalePagamenti>23.00</importoTotalePagamenti>"));
    assertTrue(xml.contains("<datiSingoliPagamenti>"));
  }

  @Test
  void givenDirectoryAsXmlOutputWhenMarshalThenFailWithIoError() {
    FlussoRiversamento validFlow = flow();

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> marshaller.marshal(validFlow, tempDir)
    );

    assertTrue(exception.getMessage().contains("Cannot create payments reporting XML"));
  }

  @Test
  void givenSchemaInvalidFlowWhenMarshalThenFailWithoutWritingXml() {
    FlussoRiversamento invalidFlow = flow();
    invalidFlow.setVersioneOggetto("2.0");
    Path xmlPath = tempDir.resolve("FLOW_1.xml");

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> marshaller.marshal(invalidFlow, xmlPath)
    );

    assertTrue(exception.getMessage().contains("does not conform to the XSD"));
    assertFalse(Files.exists(xmlPath));
  }

  private FlussoRiversamento flow() {
    CtIdentificativoUnivoco mittente = new CtIdentificativoUnivoco();
    mittente.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco.B);
    mittente.setCodiceIdentificativoUnivoco("PPAYITR1XXX");
    CtIstitutoMittente istitutoMittente = new CtIstitutoMittente();
    istitutoMittente.setIdentificativoUnivocoMittente(mittente);
    istitutoMittente.setDenominazioneMittente("Poste Pay");
    CtIdentificativoUnivocoPersonaG ricevente = new CtIdentificativoUnivocoPersonaG();
    ricevente.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersG.G);
    ricevente.setCodiceIdentificativoUnivoco("80050050154");
    CtIstitutoRicevente istitutoRicevente = new CtIstitutoRicevente();
    istitutoRicevente.setIdentificativoUnivocoRicevente(ricevente);
    istitutoRicevente.setDenominazioneRicevente("Organization");
    CtDatiSingoliPagamenti detail = new CtDatiSingoliPagamenti();
    detail.setIdentificativoUnivocoVersamento("IUV_1");
    detail.setIdentificativoUnivocoRiscossione("IUR_1");
    detail.setIndiceDatiSingoloPagamento(1);
    detail.setSingoloImportoPagato(new BigDecimal("23.00"));
    detail.setCodiceEsitoSingoloPagamento("0");
    detail.setDataEsitoSingoloPagamento(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar("2026-01-15"));
    FlussoRiversamento flow = new FlussoRiversamento();
    flow.setVersioneOggetto("1.1");
    flow.setIdentificativoFlusso("FLOW_1");
    flow.setDataOraFlusso(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar("2026-01-15T10:30:00"));
    flow.setIdentificativoUnivocoRegolamento("REG_1");
    flow.setDataRegolamento(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar("2026-01-15"));
    flow.setIstitutoMittente(istitutoMittente);
    flow.setCodiceBicBancaDiRiversamento("PPAYITR1XXX");
    flow.setIstitutoRicevente(istitutoRicevente);
    flow.setNumeroTotalePagamenti(BigDecimal.ONE);
    flow.setImportoTotalePagamenti(new BigDecimal("23.00"));
    flow.getDatiSingoliPagamentis().add(detail);
    return flow;
  }
}
