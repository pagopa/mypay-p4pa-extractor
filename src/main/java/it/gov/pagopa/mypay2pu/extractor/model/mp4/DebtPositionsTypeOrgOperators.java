package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record DebtPositionsTypeOrgOperators(
  String organizationIpaCode,
  String operatorFiscalCode,
  String debtPositionsTypeOrgCode
) implements ExportModel {

  @Override
  public String logicalKey() {
    return debtPositionsTypeOrgCode + "|" + operatorFiscalCode;
  }
}
