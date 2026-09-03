package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionpaid;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionPaidDTO;
import it.gov.pagopa.mypay2pu.extractor.exception.CsvRowMappingException;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DebtPositionPaidMapperTest {

  private final PodamFactory podamFactory = new PodamFactoryImpl();
  private final DebtPositionPaidMapper mapper = new DebtPositionPaidMapper();

  @TempDir
  private Path tempDir;

  @Test
  void mapShouldPopulateCsvDtoIncludingIufAndReceiptFields() {
    DebtPositionPaid debtPositionPaid = completeDebtPositionPaid();
    debtPositionPaid.setIuf("IUF-1");
    debtPositionPaid.setCodFiscalePa1("CF-PA1");
    debtPositionPaid.setDeNomePa1("PA One");
    debtPositionPaid.setCodTassonomicoDovutoPa1("9/0101101IM/");

    PuDebtPositionPaidDTO result = mapper.map(debtPositionPaid);

    assertEquals("IUF-1", result.getIuf());
    assertEquals(debtPositionPaid.getCodRpSilinviarpIdUnivocoVersamento(), result.getCodIuv());
    assertEquals(debtPositionPaid.getNumEDatiPagImportoTotalePagato(), result.getImportoTotalePagato());
    assertEquals(debtPositionPaid.getDtEDataOraMessaggioRicevuta(), result.getDataOraMessaggioRicevuta());
    assertEquals("CF-PA1", result.getCodFiscalePa1());
    assertEquals("PA One", result.getDeNomePa1());
    assertEquals("9/0101101IM/", result.getCodTassonomicoDovutoPa1());
    TestUtils.checkNotNullFields(result);
  }

  @Test
  void mapShouldPreserveNullOptionalFields() {
    DebtPositionPaid debtPositionPaid = new DebtPositionPaid();
    debtPositionPaid.setDtEDataOraMessaggioRicevuta(LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30));
    debtPositionPaid.setNumEDatiPagImportoTotalePagato(BigDecimal.TEN);

    PuDebtPositionPaidDTO result = mapper.map(debtPositionPaid);

    assertNull(result.getIuf());
    assertNull(result.getCodiceContestoPagamento());
    assertNull(result.getDataEsitoSingoloPagamento());
    assertNull(result.getCodFiscalePa1());
    assertNull(result.getDeNomePa1());
    assertNull(result.getCodTassonomicoDovutoPa1());
    assertEquals(LocalDateTime.of(2026, Month.JANUARY, 15, 10, 30), result.getDataOraMessaggioRicevuta());
    assertEquals(BigDecimal.TEN, result.getImportoTotalePagato());
  }

  @Test
  void mapShouldRejectUnsupportedBeneficiaryEntityIdType() {
    DebtPositionPaid debtPositionPaid = new DebtPositionPaid();
    debtPositionPaid.setCodEEnteBenefIdUnivBenefTipoIdUnivoco('F');

    CsvRowMappingException exception = assertThrows(
      CsvRowMappingException.class,
      () -> mapper.map(debtPositionPaid)
    );

    assertEquals("enteBenefTipoIdentificativoUnivoco", exception.getField());
    assertEquals("F", exception.getRejectedValue());
  }

  @Test
  void csvShouldUseHistoricalHeaderSemicolonSeparatorAndUtf8() throws IOException {
    Path csvPath = tempDir.resolve("debt-positions-paid.csv");
    DebtPositionPaid debtPositionPaid = completeDebtPositionPaid();
    debtPositionPaid.setIuf("IUF-1");
    debtPositionPaid.setDeNomePa1("Pà Uno");
    debtPositionPaid.setBlbRtPayload(null);
    debtPositionPaid.setBlbEDatiPagDatiSingPagAllegatoRicevutaTest(null);
    PuDebtPositionPaidDTO dto = mapper.map(debtPositionPaid);
    AtomicBoolean supplied = new AtomicBoolean();

    new CsvService(';', '"').createCsv(csvPath, PuDebtPositionPaidDTO.class, () ->
      supplied.compareAndSet(false, true) ? List.of(dto) : List.of(), "");

    List<String> rows = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
    String[] header = rows.getFirst().split(";", -1);
    int receiptNameIndex = Arrays.asList(header).indexOf("de_nome_pa1");

    assertEquals("iuf", header[0]);
    assertTrue(receiptNameIndex > 0);
    assertEquals(2, rows.size());
    assertEquals("IUF-1", dto.getIuf());
    assertEquals("Pà Uno", dto.getDeNomePa1());
    String csvContent = Files.readString(csvPath, StandardCharsets.UTF_8);
    assertTrue(csvContent.contains("Pà Uno"), csvContent);
  }

  private DebtPositionPaid completeDebtPositionPaid() {
    DebtPositionPaid debtPositionPaid = podamFactory.manufacturePojo(DebtPositionPaid.class);
    debtPositionPaid.setNumRigaFlusso(1L);
    debtPositionPaid.setCodEEnteBenefIdUnivBenefTipoIdUnivoco('G');
    debtPositionPaid.setCodESoggVersIdUnivVersTipoIdUnivoco('F');
    debtPositionPaid.setCodESoggPagIdUnivPagTipoIdUnivoco('F');
    return debtPositionPaid;
  }
}
