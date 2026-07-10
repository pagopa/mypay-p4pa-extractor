package it.gov.pagopa.mypay2pu.extractor.enums;

public enum ExportFileVersion {
  V1_0("1_0");

  final String value;

  ExportFileVersion(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
