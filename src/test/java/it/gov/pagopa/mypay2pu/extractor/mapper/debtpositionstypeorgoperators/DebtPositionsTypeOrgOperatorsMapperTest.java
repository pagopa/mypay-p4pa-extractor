package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionstypeorgoperators;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionsTypeOrgOperatorsDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DebtPositionsTypeOrgOperatorsMapperTest {

  private final DebtPositionsTypeOrgOperatorsMapper mapper = new DebtPositionsTypeOrgOperatorsMapper();

  @Test
  void whenMapThenReturnPuDebtPositionsTypeOrgOperatorsDTO() {
    DebtPositionsTypeOrgOperators dptoo = TestUtils.getPodamFactory().manufacturePojo(DebtPositionsTypeOrgOperators.class);

    PuDebtPositionsTypeOrgOperatorsDTO result = mapper.map(dptoo);

    assertEquals(dptoo.organizationIpaCode(), result.getOrganizationIpaCode());
    assertEquals(dptoo.debtPositionsTypeOrgCode(), result.getDebtPositionsTypeOrgCode());
    assertEquals(dptoo.operatorFiscalCode(), result.getOperatorFiscalCode());

    TestUtils.reflectionEqualsByName(result, dptoo);
    TestUtils.checkNotNullFields(result);
  }
}
