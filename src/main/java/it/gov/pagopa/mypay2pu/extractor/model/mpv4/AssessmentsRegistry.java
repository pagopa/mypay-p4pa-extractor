package it.gov.pagopa.mypay2pu.extractor.model.mpv4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record AssessmentsRegistry(
  String organizationIpaCode,
  String debtPositionTypeOrgCode,
  String sectionCode,
  String sectionDescription,
  String officeCode,
  String officeDescription,
  String assessmentCode,
  String assessmentDescription,
  String operatingYear,
  String status
) implements ExportModel {

  @Override
  public String logicalKey() {  return debtPositionTypeOrgCode;  }
}
