package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record Assessments(
  String assessmentName,
  String organizationIpaCode,
  String debtPositionTypeOrgCode,
  String iuv,
  String iud,
  String officeCode,
  String officeDescription,
  String sectionCode,
  String sectionDescription,
  String assessmentCode,
  String assessmentDescription,
  Long amountCents,
  Boolean amountSubmitted
) implements ExportModel {

  @Override
  public String logicalKey() {  return assessmentName;  }
}
