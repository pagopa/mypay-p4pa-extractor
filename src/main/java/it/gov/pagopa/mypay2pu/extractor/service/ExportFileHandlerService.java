package it.gov.pagopa.mypay2pu.extractor.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

@Slf4j
@Service
public class ExportFileHandlerService {

  private final DataExportFacadeService dataExportFacadeService;
  private final ExportFileStatusService exportFileStatusService;

  public ExportFileHandlerService(DataExportFacadeService dataExportFacadeService, ExportFileStatusService exportFileStatusService) {
    this.dataExportFacadeService = dataExportFacadeService;
    this.exportFileStatusService = exportFileStatusService;
  }

  @Async("extractorTaskExecutor")
  public void executeExport(String extractionId, ExtractionRequest request) {
    log.info("Processing extraction {} for organization {} and fileTypes {}", extractionId, request.getIpaCode(), request.getFileTypes());

    OffsetDateTime now = OffsetDateTime.now(ZONEID);
    ExtractionStatusResponse newStatus = ExtractionStatusResponse.builder()
      .extractionId(extractionId)
      .ipaCode(request.getIpaCode())
      .fileTypes(request.getFileTypes())
      .status(ExtractionStatus.RUNNING)
      .createdAt(now)
      .updatedAt(now)
      .error(null)
      .files(List.of())
      .build();
    exportFileStatusService.writeStatus(newStatus);

    ExportFileResult exportFileResult;
    try {
      exportFileResult = dataExportFacadeService.executeExport(extractionId, request.getFileTypes());
    } catch (Exception e) {
      log.error("Error processing extraction {}", extractionId, e);
      exportFileResult = new ExportFileResult(List.of(), e.getMessage());
    }
    updateExportFileWithProcessingResult(extractionId, exportFileResult);
  }

  private void updateExportFileWithProcessingResult(String extractionId, ExportFileResult exportFileResult) {
    ExtractionStatusResponse currentStatus = exportFileStatusService.readStatus(extractionId);
    List<String> exportedFiles = exportFileResult.files() == null ? List.of() : List.copyOf(exportFileResult.files());
    String errorDescription = exportFileResult.error();
    if (StringUtils.isBlank(errorDescription)) {
      currentStatus
        .status(ExtractionStatus.COMPLETED)
        .updatedAt(OffsetDateTime.now(ZONEID))
        .error(null)
        .files(exportedFiles);
    } else {
      currentStatus
        .status(ExtractionStatus.FAILED)
        .updatedAt(OffsetDateTime.now(ZONEID))
        .error(errorDescription)
        .files(exportedFiles);
    }
    exportFileStatusService.writeStatus(currentStatus);
  }
}
