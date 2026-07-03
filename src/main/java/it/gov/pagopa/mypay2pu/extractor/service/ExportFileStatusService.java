package it.gov.pagopa.mypay2pu.extractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ExportFileStatusService {

  private static final String STATUS_FILE_NAME = "status.json";

  private final ObjectMapper objectMapper;
  private final Path storagePath;

  public ExportFileStatusService(ObjectMapper objectMapper, ExtractorExportProperties extractorExportProperties) {
    this.objectMapper = objectMapper;
    this.storagePath = Paths.get(extractorExportProperties.storagePath());
  }

  public ExtractionStatusResponse readStatus(String extractionId) {
    Path statusPath = resolveStatusPath(extractionId);
    if (!Files.exists(statusPath)) {
      throw new ExportFileNotFoundException("File for extractionId: %s not found".formatted(extractionId));
    }
    try {
      return objectMapper.readValue(statusPath.toFile(), ExtractionStatusResponse.class);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read status file for extraction %s".formatted(extractionId), e);
    }
  }

  public void writeStatus(ExtractionStatusResponse status) {
    try {
      Files.createDirectories(resolveExtractionDirectory(status.getExtractionId()));
      objectMapper.writeValue(resolveStatusPath(status.getExtractionId()).toFile(), status);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot write status file for extraction %s".formatted(status.getExtractionId()), e);
    }
  }

  public Path resolveExtractionDirectory(String extractionId) {
    return storagePath.resolve(extractionId);
  }

  private Path resolveStatusPath(String extractionId) {
    return resolveExtractionDirectory(extractionId).resolve(STATUS_FILE_NAME);
  }
}
