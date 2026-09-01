package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record DebtPositionType(
  String brokerCf,
  String debtPositionTypeCode,
  String description,
  String orgType,
  String macroArea,
  String serviceType,
  String collectingReason,
  String taxonomyCode,
  Boolean flagAnonymousFiscalCode,
  Boolean flagMandatoryDueDate,
  Boolean flagNotifyIo
) implements ExportModel {

  @Override
  public String logicalKey() {
    return debtPositionTypeCode;
  }
}
