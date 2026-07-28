package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record ExportFileNameBuilder(
  String brokerIpaCode,
  String ipaCodeOrganization,
  MigrationFileType migrationFileType,
  LocalDateTime timestamp,
  String version
) {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  public String buildBaseName() {
    return buildBaseName(null);
  }

  public String buildCsvFileName() {
    return buildBaseName() + ".csv";
  }

  public String buildCsvPartFileName(int partNumber) {
    return buildBaseName(partNumber) + ".csv";
  }

  private String buildBaseName(Integer partNumber) {
    String migrationType = migrationFileType.name()
      .toUpperCase(Locale.ROOT);

    String formattedTimestamp = timestamp.format(TIMESTAMP_FORMATTER);

    String ipaCodeFile = migrationFileType == MigrationFileType.ORGANIZATIONS ? brokerIpaCode : ipaCodeOrganization;

    if (partNumber == null) {
      return "%s-%s-%s-%s".formatted(
        ipaCodeFile,
        migrationType,
        formattedTimestamp,
        version
      );
    }

    if (partNumber <= 0) {
      throw new IllegalArgumentException("Part number must be positive");
    }

    return "%s-%s-%s-part%03d-%s".formatted(
      ipaCodeFile,
      migrationType,
      formattedTimestamp,
      partNumber,
      version
    );
  }
}
