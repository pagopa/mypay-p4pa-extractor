package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionstypeorgoperators;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionsTypeOrgOperatorsDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionsTypeOrgOperatorsMapper {

  public PuDebtPositionsTypeOrgOperatorsDTO map(DebtPositionsTypeOrgOperators debtPositionsTypeOrgOperators) {
    return PuDebtPositionsTypeOrgOperatorsDTO.builder()
      .organizationIpaCode(debtPositionsTypeOrgOperators.organizationIpaCode())
      .operatorFiscalCode(debtPositionsTypeOrgOperators.operatorFiscalCode())
      .debtPositionsTypeOrgCode(debtPositionsTypeOrgOperators.debtPositionsTypeOrgCode())
      .build();
  }
}
