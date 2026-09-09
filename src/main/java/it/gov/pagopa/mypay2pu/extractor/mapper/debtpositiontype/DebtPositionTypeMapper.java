package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontype;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionTypeMapper {

  private static final String DEFAULT_DEBT_POSITION_TYPE_CODE = "DEFAULT";
  private static final String UNKNOWN_DEBT_POSITION_TYPE_CODE = "UNKNOWN";

  public PuDebtPositionTypeDTO map(DebtPositionType debtPositionType) {
    return PuDebtPositionTypeDTO.builder()
      .brokerCf(debtPositionType.brokerCf())
      .debtPositionTypeCode(mapDebtPositionTypeCode(debtPositionType.debtPositionTypeCode()))
      .description(debtPositionType.description())
      .orgType(debtPositionType.orgType())
      .macroArea(debtPositionType.macroArea())
      .serviceType(debtPositionType.serviceType())
      .collectingReason(debtPositionType.collectingReason())
      .taxonomyCode(debtPositionType.taxonomyCode())
      .flagAnonymousFiscalCode(debtPositionType.flagAnonymousFiscalCode())
      .flagMandatoryDueDate(debtPositionType.flagMandatoryDueDate())
      .flagNotifyIo(debtPositionType.flagNotifyIo())
      .ioTemplateMessage(null)
      .ioTemplateSubject(null)
      .build();
  }

  private String mapDebtPositionTypeCode(String debtPositionTypeCode) {
    return DEFAULT_DEBT_POSITION_TYPE_CODE.equals(debtPositionTypeCode)
      ? UNKNOWN_DEBT_POSITION_TYPE_CODE
      : debtPositionTypeCode;
  }
}
