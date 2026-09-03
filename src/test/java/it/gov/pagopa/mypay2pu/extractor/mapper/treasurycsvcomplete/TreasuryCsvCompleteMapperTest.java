package it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.dto.export.TreasuryCsvCompleteDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TreasuryCsvCompleteMapperTest {

  private final TreasuryCsvCompleteMapper mapper = new TreasuryCsvCompleteMapper();

  @Test
  void mapShouldPopulateAllTreasuryCsvCompleteFields() {
    TreasuryCsvComplete treasury = treasury(
      "1500.25",
      "123",
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 1),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 2),
      true,
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 3)
    );

    TreasuryCsvCompleteDTO result = mapper.map(treasury);

    TestUtils.checkNotNullFields(result, "codEnteBT", "codIstatEnte");
    assertNull(result.getCodEnteBT());
    assertNull(result.getCodIstatEnte());
    assertEquals("IPA1", result.getEnteIpaCode());
    assertEquals(new BigDecimal("1500.25"), result.getImportoCentesimi());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getDataBolletta());
    assertEquals(LocalDateTime.of(2026, Month.JANUARY, 10, 10, 1), result.getDataRicezione());
    assertEquals(123L, result.getNumPgProcesso());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getDataValutaRegione());
    assertEquals(true, result.getFlgRegolarizzata());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getDataEffettivaSospeso());
    assertEquals("2026|BOLLETTA-1", mapper.buildLogicalKey(treasury));
  }

  @Test
  void mapShouldPreserveNullValues() {
    TreasuryCsvCompleteDTO result = mapper.map(treasury(null, null, null, null, null, null, null));

    TestUtils.checkNotNullFields(result,
      "codEnteBT", "codIstatEnte", "importoCentesimi", "dataBolletta", "dataRicezione", "numPgProcesso",
      "dataValutaRegione", "flgRegolarizzata", "dataEffettivaSospeso"
    );
    assertNull(result.getImportoCentesimi());
    assertNull(result.getNumPgProcesso());
    assertNull(result.getDataBolletta());
    assertNull(result.getDataRicezione());
    assertNull(result.getDataValutaRegione());
    assertNull(result.getFlgRegolarizzata());
    assertNull(result.getDataEffettivaSospeso());
  }

  private TreasuryCsvComplete treasury(
    String importoCentesimi,
    String numPgProcesso,
    LocalDateTime dataBolletta,
    LocalDateTime dataRicezione,
    LocalDateTime dataValutaRegione,
    Boolean flgRegolarizzata,
    LocalDateTime dataEffettivaSospeso
  ) {
    return new TreasuryCsvComplete(
      "2026", "BOLLETTA-1", "SOURCE-BT", "SOURCE-ISTAT", "IPA1", "IUF", "IUV", "CONTO", "DOMINIO", "MOVIMENTO",
      "CAUSALE", "Causale", importoCentesimi, dataBolletta, dataRicezione, "2026", "DOCUMENTO", "BOLLO", "Cognome",
      "Nome", "Via", "00100", "Roma", "CF", "PIVA", "ABI", "CAB", "IBAN", "CONTO-ANAGRAFICA", "Provvisorio",
      "COD-PROVVISORIO", "TIPO-CONTO", "PROCESSO", "PG-ESECUZIONE", "PG-TRASFERIMENTO", numPgProcesso,
      dataValutaRegione, flgRegolarizzata, dataEffettivaSospeso, "GESTIONALE", "END-TO-END"
    );
  }
}
