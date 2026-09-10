package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentsReportingDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFileNameBuilder;
import it.gov.pagopa.mypay2pu.extractor.service.export.PaginatedExportRowsSupplier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

@Service
public class PaymentsReportingExportProcessingService
  extends BaseExportProcessingService<String> {

  private static final String ZIP_VERSION = "1.0";
  private final PaymentsReportingDao paymentsReportingDao;

  public PaymentsReportingExportProcessingService(PaymentsReportingDao paymentsReportingDao,
                                                   PaymentsReportingPartitionWriterService partitionWriterService,
                                                   FileArchiverService fileArchiverService,
                                                   ExtractorExportProperties exportProperties) {
    super(partitionWriterService, fileArchiverService, exportProperties);
    this.paymentsReportingDao = paymentsReportingDao;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.PAYMENTS_REPORTING;
  }

  @Override
  protected String getZipVersion() {
    return ZIP_VERSION;
  }

  @Override
  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    List<String> archiveNames = new ArrayList<>();
    for (String ipaCode : request.getIpaCodes()) {
      ExtractionRequest singleIpaRequest = new ExtractionRequest(
        List.of(ipaCode), request.getFileTypes(), request.getLastExtractionDate(), request.getFilters()
      );
      archiveNames.addAll(super.executeExport(extractionId, singleIpaRequest).files());
    }
    return new ExportFileResult(archiveNames, null);
  }

  @Override
  protected boolean useBrokerIpaAsPrefix() {
    return false;
  }

  @Override
  protected String getArchiveBaseName(ExportFileNameBuilder fileNameBuilder) {
    return fileNameBuilder.buildOrganizationZipBaseName();
  }

  @Override
  protected ExportGenerationResult generateExport(ExtractionRequest request, Path workingDirectory,
                                                   int pageSize, ExportFileNameBuilder fileNameBuilder)
    throws IOException {
    ExtractionFilters filters = request.getFilters();
    String logicalKey = filters != null ? filters.getLogicalKey() : null;
    OffsetDateTime dateFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime dateTo = filters != null ? filters.getDateTo() : null;
    Supplier<List<String>> rows = new PaginatedExportRowsSupplier<>(
      (limit, offset) -> retrieveFiles(request.getIpaCodes().getFirst(), request.getLastExtractionDate(),
        logicalKey, dateFrom, dateTo, limit, offset),
      pageSize
    );
    return new ExportGenerationResult(
      partitionWriter().writePartitions(workingDirectory, fileNameBuilder, String.class, rows, ZIP_VERSION, pageSize),
      List.of()
    );
  }

  private List<String> retrieveFiles(String ipaCode, OffsetDateTime lastExtractionDate, String logicalKey, OffsetDateTime dateFrom,
                                     OffsetDateTime dateTo, int limit, int offset) {
    return StringUtils.hasText(logicalKey)
      ? paymentsReportingDao.findByLogicalKey(ipaCode, logicalKey, limit, offset)
      : paymentsReportingDao.findByDateRange(ipaCode, lastExtractionDate, dateFrom, dateTo, limit, offset);
  }

}
