package it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuTreasuryCsvCompleteDTO;
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
      new BigDecimal("1500.25"),
      123L,
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 0),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 1),
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 2),
      true,
      LocalDateTime.of(2026, Month.JANUARY, 10, 10, 3)
    );

    PuTreasuryCsvCompleteDTO result = mapper.map(treasury);

    TestUtils.checkNotNullFields(result, "orgBtCode", "orgIstatCode");
    assertNull(result.getOrgBtCode());
    assertNull(result.getOrgIstatCode());
    assertEquals("IPA1", result.getOrganizationIpaCode());
    assertEquals(150025L, result.getBillAmountCents());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getBillDate());
    assertEquals(LocalDateTime.of(2026, Month.JANUARY, 10, 10, 1), result.getReceptionDate());
    assertEquals(123L, result.getProcessPgNumber());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getRegionValueDate());
    assertEquals(true, result.getIsRegularized());
    assertEquals(LocalDate.of(2026, Month.JANUARY, 10), result.getActualSuspensionDate());
    assertEquals("2026|BOLLETTA-1", treasury.logicalKey());
    assertEquals(
      false,
      mapper.map(treasury(      new BigDecimal("1500.25"), 123L, treasury.dtBolletta(), treasury.dtRicezione(),
        treasury.dtDataValutaRegione(), false, treasury.dtEffettivaSospeso())).getIsRegularized()
    );
  }

  @Test
  void mapShouldPreserveNullValues() {
    PuTreasuryCsvCompleteDTO result = mapper.map(treasury(null, null, null, null, null, null, null));

    TestUtils.checkNotNullFields(result,
      "orgBtCode", "orgIstatCode", "billAmountCents", "billDate", "receptionDate", "processPgNumber",
      "regionValueDate", "isRegularized", "actualSuspensionDate"
    );
    assertNull(result.getBillAmountCents());
    assertNull(result.getProcessPgNumber());
    assertNull(result.getBillDate());
    assertNull(result.getReceptionDate());
    assertNull(result.getRegionValueDate());
    assertNull(result.getIsRegularized());
    assertNull(result.getActualSuspensionDate());
  }

  private TreasuryCsvComplete treasury(
    BigDecimal importoCentesimi,
    Long numPgProcesso,
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
