package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dto.ExportFileResult;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

/**
 * Coordinates common export lifecycle concerns: temporary storage, naming,
 * archiving, and cleanup.
 *
 * <p>Format-specific subclasses generate the artifacts through their
 * {@link PartitionWriterService} and return them to this class for final
 * archiving.</p>
 *
 * @param <C> writer item type
 */
public abstract class BaseExportProcessingService<C> {

  private final PartitionWriterService<C> partitionWriterService;
  private final FileArchiverService fileArchiverService;
  private final ExtractorExportProperties exportProperties;

  protected BaseExportProcessingService(PartitionWriterService<C> partitionWriterService,
                                        FileArchiverService fileArchiverService,
                                        ExtractorExportProperties exportProperties) {
    this.partitionWriterService = partitionWriterService;
    this.fileArchiverService = fileArchiverService;
    this.exportProperties = exportProperties;
  }

  /**
   * Generates and archives the files for an extraction request.
   *
   * @param extractionId unique extraction identifier
   * @param request request containing file type, organizations, and filters
   * @return names of the archived export files
   */
  public ExportFileResult executeExport(String extractionId, ExtractionRequest request) {
    Path workingDirectory = Path.of(exportProperties.tempBaseDir()).resolve(extractionId)
      .resolve(getMigrationFileType().name().toLowerCase(Locale.ROOT));
    ExportFileNameBuilder fileNameBuilder = new ExportFileNameBuilder(
      exportProperties.brokerIpaCode(), request.getIpaCodes().getFirst(), useBrokerIpaAsPrefix(),
      getMigrationFileType(), LocalDateTime.now(ZONEID), getZipVersion()
    );
    try {
      ExportGenerationResult result = generateExport(
        request, workingDirectory,
        exportProperties.resolveFileTypeConfiguration(getMigrationFileType()).exportPageSize(),
        fileNameBuilder
      );
      return new ExportFileResult(archive(result, fileNameBuilder, extractionId, workingDirectory), null);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot generate export for " + getMigrationFileType(), e);
    } finally {
      cleanupWorkingDirectory(workingDirectory);
    }
  }

  private List<String> archive(ExportGenerationResult result, ExportFileNameBuilder nameBuilder,
                               String extractionId, Path workingDirectory) throws IOException {
    List<String> names = new ArrayList<>(2);
    Path targetDirectory = Path.of(exportProperties.storagePath()).resolve(extractionId);
    List<List<Path>> fileGroups = result.fileGroups();
    List<String> archiveBaseNames = getArchiveBaseNames(nameBuilder, fileGroups.size());
    for (int groupIndex = 0; groupIndex < fileGroups.size(); groupIndex++) {
      Path zipPath = workingDirectory.resolve(archiveBaseNames.get(groupIndex) + ".zip");
      fileArchiverService.compressAndArchive(fileGroups.get(groupIndex), zipPath, targetDirectory);
      names.add(zipPath.getFileName().toString());
    }
    String archiveBaseName = getArchiveBaseName(nameBuilder);
    if (!result.errorFiles().isEmpty()) {
      Path errorZipPath = workingDirectory.resolve(archiveBaseName + ".errors.zip");
      fileArchiverService.compressAndArchive(result.errorFiles(), errorZipPath, targetDirectory);
      names.add(errorZipPath.getFileName().toString());
    }
    return names;
  }

  private void cleanupWorkingDirectory(Path workingDirectory) {
    if (Files.exists(workingDirectory) && !FileSystemUtils.deleteRecursively(workingDirectory.toFile())) {
      throw new IllegalStateException("Cannot clean temporary export directory " + workingDirectory);
    }
  }

  /**
   * Returns the writer used by the format-specific generation strategy.
   *
   * @return configured partition writer
   */
  protected PartitionWriterService<C> partitionWriter() {
    return partitionWriterService;
  }

  /**
   * Selects whether archive names use the broker IPA code as their prefix.
   *
   * @return {@code true} when the broker IPA code is used
   */
  protected boolean useBrokerIpaAsPrefix() {
    return true;
  }

  /**
   * Builds the base name of the final archive.
   *
   * @param fileNameBuilder builder initialized for the current export
   * @return archive base name without extension
   */
  protected String getArchiveBaseName(ExportFileNameBuilder fileNameBuilder) {
    return fileNameBuilder.buildZipBaseName();
  }

  protected List<String> getArchiveBaseNames(ExportFileNameBuilder fileNameBuilder, int totalParts) {
    List<String> archiveBaseNames = new ArrayList<>(totalParts);
    for (int ignored = 0; ignored < totalParts; ignored++) {
      archiveBaseNames.add(getArchiveBaseName(fileNameBuilder));
    }
    return archiveBaseNames;
  }

  /** @return migration file type handled by the implementation */
  protected abstract MigrationFileType getMigrationFileType();

  /** @return format version included in generated names */
  protected abstract String getZipVersion();

  /**
   * Generates temporary export artifacts for the current request.
   *
   * @param request extraction request
   * @param workingDirectory temporary output directory
   * @param pageSize retrieval and partition size
   * @param fileNameBuilder builder for artifact names
   * @return generated export and error artifacts
   * @throws IOException if artifact generation fails
   */
  protected abstract ExportGenerationResult generateExport(ExtractionRequest request, Path workingDirectory,
                                                           int pageSize, ExportFileNameBuilder fileNameBuilder)
    throws IOException;

  /**
   * Artifacts produced by a format-specific export generation.
   *
   * @param fileGroups normal export file groups
   * @param errorFiles optional error-report files
   */
  public record ExportGenerationResult(List<List<Path>> fileGroups, List<Path> errorFiles) { }
}
