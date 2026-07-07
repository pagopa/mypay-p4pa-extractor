package it.gov.pagopa.mypay2pu.extractor.dao;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ExportFileStatusDao {

  private static final String STATUS_FILE_NAME = "status.json";

  private final JsonMapper jsonMapper;
  private final Path storagePath;

  public ExportFileStatusDao(JsonMapper jsonMapper,
                             @Value("${extractor.export.storage-path}") String storagePath) {
    this.jsonMapper = jsonMapper;
    this.storagePath = Paths.get(storagePath);
  }

  public ExtractionStatusResponse readStatus(String extractionId) {
    Path statusPath = resolveStatusPath(extractionId);
    if (!Files.exists(statusPath)) {
      throw new ExportFileNotFoundException("File for extractionId: %s not found".formatted(extractionId));
    }
    try {
      return jsonMapper.readValue(statusPath.toFile(), ExtractionStatusResponse.class);
    } catch (JacksonIOException e) {
      throw new StreamReadException("Cannot deserialize status file for extraction %s".formatted(extractionId));
    }
  }

  public void writeStatus(ExtractionStatusResponse status) {
    try {
      Files.createDirectories(resolveExtractionDirectory(status.getExtractionId()));
      jsonMapper.writeValue(resolveStatusPath(status.getExtractionId()).toFile(), status);
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
