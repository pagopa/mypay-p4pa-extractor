package it.gov.pagopa.mypay2pu.extractor.dto;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ArchiveFile;

import java.util.Collection;
import java.util.List;

public record ExportFileResult(
  List<ArchiveFile> archiveFiles,
  String error
) {

  public ExportFileResult(Collection<String> files, String error) {
    this(files == null ? null : files.stream()
      .map(file -> new ArchiveFile().name(file).files(List.of()))
      .toList(), error);
  }

  public static ExportFileResult fromArchiveFiles(List<ArchiveFile> archiveFiles, String error) {
    return new ExportFileResult(archiveFiles, error);
  }

  public List<String> files() {
    return archiveFiles == null ? null : archiveFiles.stream().map(ArchiveFile::getName).toList();
  }
}
