package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
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
    List<String> zipFileNames = new ArrayList<>();
    for (String organizationId : request.getIpaCodes()) {
      zipFileNames.add(createZip(extractionId, organizationId, request.getFilters()));
    }
    return new ExportFileResult(zipFileNames, null);
  }

  private String createZip(String extractionId, String organizationId, ExtractionFilters filters) {
    String logicalKey = filters != null ? filters.getLogicalKey() : null;
    OffsetDateTime createdFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime createdTo = filters != null ? filters.getDateTo() : null;

    log.info(
      "Exporting payments reporting: organizationId={}, logicalKey={}, createdFrom={}, createdTo={}",
      organizationId, logicalKey, createdFrom, createdTo
    );

    List<Path> records = StringUtils.hasText(logicalKey)
      ? paymentsReportingDao.findByLogicalKey(organizationId, logicalKey)
      : paymentsReportingDao.findByDateRange(organizationId, createdFrom, createdTo);
    log.info("Found {} payments reporting records for organizationId={}", records.size(), organizationId);

    List<Path> xmlFiles = resolveExistingXmlFiles(records, organizationId);
    Path zipPath = resolveZipPath(extractionId, organizationId);
    zipFileService.zipper(zipPath, xmlFiles);

    log.info(
      "Generated payments reporting ZIP: organizationId={}, zip={}, processedXmls={}, skippedXmls={}",
      organizationId, zipPath, xmlFiles.size(), records.size() - xmlFiles.size()
    );
    return zipPath.getFileName().toString();
  }

  private List<Path> resolveExistingXmlFiles(List<Path> records, String organizationId) {
    List<Path> xmlFiles = new ArrayList<>();
    Path baseDirectory = Path.of(extractorExportProperties.paymentsReporting().baseDirectory());
    for (Path filePath : records) {
      Path resolvedPath = filePath.isAbsolute() ? filePath : baseDirectory.resolve(filePath);
      if (Files.exists(resolvedPath)) {
        xmlFiles.add(resolvedPath);
      } else {
        log.error("Payments reporting XML file not found: organizationId={}, path={}", organizationId, resolvedPath);
      }
    }
    return xmlFiles;
  }

  private Path resolveZipPath(String extractionId, String organizationId) {
    Path outputDirectory = Path.of(extractorExportProperties.storagePath()).resolve(extractionId);
    try {
      Files.createDirectories(outputDirectory);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot create payments reporting output directory " + outputDirectory, e);
    }
    String timestamp = LocalDateTime.now(ZONEID).format(ZIP_TIMESTAMP_FORMATTER);
    return outputDirectory.resolve("%s_%s_%s.zip".formatted(organizationId, ZIP_VERSION, timestamp));
  }
}
