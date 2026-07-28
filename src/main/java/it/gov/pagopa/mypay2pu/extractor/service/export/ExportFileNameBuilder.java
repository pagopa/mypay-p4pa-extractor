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

  public String buildZipBaseName() {
    return buildBaseName(brokerIpaCode, null);
  }

  public String buildCsvFileName() {
    return buildBaseName(resolveCsvIpaCode(), null) + ".csv";
  }

  public String buildCsvPartFileName(int partNumber) {
    return buildBaseName(resolveCsvIpaCode(), partNumber) + ".csv";
  }

  private String buildBaseName(String ipaCode, Integer partNumber) {
    String migrationType = migrationFileType.name()
      .toUpperCase(Locale.ROOT);

    String formattedTimestamp = timestamp.format(TIMESTAMP_FORMATTER);

    if (partNumber == null) {
      return "%s-%s-%s-%s".formatted(
        ipaCode,
        migrationType,
        formattedTimestamp,
        version
      );
    }

    if (partNumber <= 0) {
      throw new IllegalArgumentException("Part number must be positive");
    }

    return "%s-%s-%s-part%03d-%s".formatted(
      ipaCode,
      migrationType,
      formattedTimestamp,
      partNumber,
      version
    );
  }

  private String resolveCsvIpaCode() {
    return migrationFileType == MigrationFileType.ORGANIZATIONS
      ? brokerIpaCode
      : ipaCodeOrganization;
  }
}
