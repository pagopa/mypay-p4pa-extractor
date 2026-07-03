package it.gov.pagopa.mypay2pu.extractor.dto;

import java.util.List;

public record ExportFileResult(
  List<String> files,
  String error
) { }
