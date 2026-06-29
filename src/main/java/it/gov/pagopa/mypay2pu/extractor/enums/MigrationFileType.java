package it.gov.pagopa.mypay2pu.extractor.enums;

public enum MigrationFileType {
  ORGANIZATIONS("1_0"),
  ORG_SIL_SERVICES("1_0"),
  DEBT_POSITIONS_TYPE("1_0"),
  DEBT_POSITIONS_TYPE_ORG("1_0"),
  DEBT_POSITIONS_TYPE_ORG_OPERATORS("1_0"),
  DEBT_POSITIONS("1_0"),
  DEBT_POSITIONS_PAID("1_0"),
  PAYMENT_NOTIFICATION("1_0"),
  PAYMENTS_REPORTING("1_0"),
  TREASURY_CSV_COMPLETE("1_0"),
  ASSESSMENTS("1_0"),
  ASSESSMENTS_REGISTRY("1_0");

  private final String zipVersion;

  MigrationFileType(String zipVersion) {
    this.zipVersion = zipVersion;
  }

  public String getZipVersion() {
    return zipVersion;
  }
}
