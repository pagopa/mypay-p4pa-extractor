package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

@Slf4j
@Service
public class PaymentsReportingExportProcessingService {

  private static final String ZIP_VERSION = "1.0";
  private static final DateTimeFormatter ZIP_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final PaymentsReportingDao paymentsReportingDao;
  private final ExtractorExportProperties extractorExportProperties;
  private final ZipFileService zipFileService;

  public PaymentsReportingExportProcessingService(PaymentsReportingDao paymentsReportingDao,
                                                   ExtractorExportProperties extractorExportProperties,
                                                   ZipFileService zipFileService) {
    this.paymentsReportingDao = paymentsReportingDao;
    this.extractorExportProperties = extractorExportProperties;
    this.zipFileService = zipFileService;
  }

  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    return new ExportFileResult(
      request.getIpaCodes().stream()
        .map(ipaCode -> createZip(extractionId, ipaCode, request.getFilters()))
        .toList(),
      null
    );
  }

  private String createZip(String extractionId, String ipaCode, ExtractionFilters filters) {
    String logicalKey = filters != null ? filters.getLogicalKey() : null;
    OffsetDateTime createdFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime createdTo = filters != null ? filters.getDateTo() : null;

    log.info(
      "Exporting payments reporting: ipaCode={}, logicalKey={}, createdFrom={}, createdTo={}",
      ipaCode, logicalKey, createdFrom, createdTo
    );

    Path zipPath = resolveZipPath(extractionId, ipaCode);
    int pageSize = extractorExportProperties
      .resolveFileTypeConfiguration(MigrationFileType.PAYMENTS_REPORTING)
      .exportPageSize();
    List<String> records = retrieveRecords(ipaCode, logicalKey, createdFrom, createdTo, pageSize);
    List<Path> xmlFiles = resolveExistingXmlFiles(records, ipaCode);
    zipFileService.zipper(zipPath, xmlFiles);

    log.info(
      "Generated payments reporting ZIP: ipaCode={}, zip={}, processedXmls={}, skippedXmls={}",
      ipaCode, zipPath, xmlFiles.size(), records.size() - xmlFiles.size()
    );
    return zipPath.getFileName().toString();
  }

  private List<String> retrieveRecords(String ipaCode,
                                       String logicalKey,
                                       OffsetDateTime createdFrom,
                                       OffsetDateTime createdTo,
                                       int pageSize) {
    List<String> records = new ArrayList<>();
    int offset = 0;
    List<String> page;
    do {
      page = StringUtils.hasText(logicalKey)
        ? paymentsReportingDao.findByLogicalKey(ipaCode, logicalKey, pageSize, offset)
        : paymentsReportingDao.findByDateRange(ipaCode, null, createdFrom, createdTo, pageSize, offset);
      records.addAll(page);
      offset += page.size();
    } while (page.size() == pageSize);
    return records;
  }

  private List<Path> resolveExistingXmlFiles(List<String> records, String ipaCode) {
    List<Path> xmlFiles = new ArrayList<>();
    Path baseDirectory = Path.of(extractorExportProperties.paymentsReporting().baseDirectory());
    for (String fileName : records) {
      Path filePath = Path.of(fileName);
      Path resolvedPath = filePath.isAbsolute() ? filePath : baseDirectory.resolve(filePath);
      if (Files.exists(resolvedPath)) {
        xmlFiles.add(resolvedPath);
      } else {
        log.error("Payments reporting XML file not found: ipaCode={}, path={}", ipaCode, resolvedPath);
      }
    }
    return xmlFiles;
  }

  private Path resolveZipPath(String extractionId, String ipaCode) {
    Path outputDirectory = Path.of(extractorExportProperties.storagePath()).resolve(extractionId);
    try {
      Files.createDirectories(outputDirectory);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot create payments reporting output directory " + outputDirectory, e);
    }
    String timestamp = LocalDateTime.now(ZONEID).format(ZIP_TIMESTAMP_FORMATTER);
    return outputDirectory.resolve("%s_%s_%s.zip".formatted(ipaCode, ZIP_VERSION, timestamp));
  }
}
