package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontype;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionTypeMapper {

  public PuDebtPositionTypeDTO map(DebtPositionType debtPositionType) {
    return PuDebtPositionTypeDTO.builder().build();
  }
}
