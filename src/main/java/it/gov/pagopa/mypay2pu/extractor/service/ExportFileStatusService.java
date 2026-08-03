package it.gov.pagopa.mypay2pu.extractor.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

@Slf4j
@Service
public class ExportFileStatusService {

  private final ExportFileStatusDao exportFileStatusDao;

  public ExportFileStatusService(ExportFileStatusDao exportFileStatusDao) {
    this.exportFileStatusDao = exportFileStatusDao;
  }

  public void createNew(String extractionId, ExtractionRequest request) {
    OffsetDateTime now = OffsetDateTime.now(ZONEID);
    ExtractionStatusResponse newStatus = ExtractionStatusResponse.builder()
      .extractionId(extractionId)
      .ipaCodes(List.copyOf(request.getIpaCodes()))
      .fileTypes(request.getFileTypes())
      .status(ExtractionStatus.RUNNING)
      .createdAt(now)
      .updatedAt(now)
      .error(null)
      .files(List.of())
      .build();
    exportFileStatusDao.writeStatus(newStatus);
  }

  public void update(String extractionId, ExportFileResult exportFileResult) {
    ExtractionStatusResponse currentStatus = exportFileStatusDao.readStatus(extractionId);
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
    exportFileStatusDao.writeStatus(currentStatus);
  }

  public ExtractionStatusResponse readStatus(String extractionId) {
    return exportFileStatusDao.readStatus(extractionId);
  }
}
