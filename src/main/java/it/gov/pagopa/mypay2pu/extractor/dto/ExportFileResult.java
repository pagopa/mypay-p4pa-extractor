package it.gov.pagopa.mypay2pu.extractor.dto;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;

import java.util.List;

public record ExportFileResult(
  List<ArchiveFile> archiveFiles,
  String error
) {

  public List<String> files() {
    return archiveFiles == null ? null : archiveFiles.stream().map(ArchiveFile::getName).toList();
  }
}
