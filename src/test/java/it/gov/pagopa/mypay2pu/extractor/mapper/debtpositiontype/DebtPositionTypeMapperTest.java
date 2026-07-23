package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontype;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DebtPositionTypeMapperTest {

  private final DebtPositionTypeMapper debtPositionTypeMapper = new DebtPositionTypeMapper();

  @Test
  void mapShouldPopulateExportDto() {
    DebtPositionType debtPositionType = new DebtPositionType(
      "12345678901",
      "TAX",
      "Tax",
      "COMUNE",
      "AREA",
      "SERVICE",
      "REASON",
      "01.01.01",
      false,
      false,
      false
    );

    PuDebtPositionTypeDTO result = debtPositionTypeMapper.map(debtPositionType);

    assertEquals(debtPositionType.brokerCf(), result.getBrokerCf());
    assertEquals(debtPositionType.debtPositionTypeCode(), result.getDebtPositionTypeCode());
    assertEquals(debtPositionType.description(), result.getDescription());
    assertEquals(debtPositionType.orgType(), result.getOrgType());
    assertEquals(debtPositionType.macroArea(), result.getMacroArea());
    assertEquals(debtPositionType.serviceType(), result.getServiceType());
    assertEquals(debtPositionType.collectingReason(), result.getCollectingReason());
    assertEquals(debtPositionType.taxonomyCode(), result.getTaxonomyCode());
    assertEquals(debtPositionType.flagAnonymousFiscalCode(), result.getFlagAnonymousFiscalCode());
    assertEquals(debtPositionType.flagMandatoryDueDate(), result.getFlagMandatoryDueDate());
    assertEquals(debtPositionType.flagNotifyIo(), result.getFlagNotifyIo());
    TestUtils.reflectionEqualsByName(result, debtPositionType);
    TestUtils.checkNotNullFields(result, "ioTemplateSubject", "ioTemplateMessage");
  }
}
