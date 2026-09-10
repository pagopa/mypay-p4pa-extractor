package it.gov.pagopa.mypay2pu.extractor.service.export.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFileNameBuilder;
import it.gov.pagopa.mypay2pu.extractor.service.export.PartitionWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
public class PaymentsReportingPartitionWriterService implements PartitionWriterService<String> {

  private final Path myPayDirectory;

  public PaymentsReportingPartitionWriterService(
    it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties exportProperties
  ) {
    this.myPayDirectory = Path.of(exportProperties.paymentsReporting().baseDirectory());
  }

  @Override
  public <T extends String> List<Path> writePartitions(Path workingDirectory,
                                                        ExportFileNameBuilder fileNameBuilder,
                                                        Class<T> itemClass,
                                                        Supplier<? extends List<? extends T>> sourceSupplier,
                                                        String outputProfile,
                                                        int maxItemsPerPart) throws IOException {
    if (maxItemsPerPart <= 0) {
      throw new IllegalArgumentException("Max items per part must be positive");
    }
    Files.createDirectories(workingDirectory);
    List<Path> copiedFiles = new ArrayList<>();
    List<? extends T> files;
    while (!(files = sourceSupplier.get()).isEmpty()) {
      copiedFiles.addAll(copyFiles(workingDirectory, List.copyOf(files)));
    }
    return List.copyOf(copiedFiles);
  }

  public List<Path> copyFiles(Path workingDirectory, List<String> fileNames) throws IOException {
    Files.createDirectories(workingDirectory);
    List<Path> copiedFiles = new ArrayList<>();
    for (String fileName : fileNames) {
      Path source = Path.of(fileName);
      source = source.isAbsolute() ? source : myPayDirectory.resolve(source);
      if (!Files.isRegularFile(source)) {
        log.error("Payments reporting XML file not found: {}", source);
        continue;
      }
      Path target = workingDirectory.resolve(source.getFileName());
      if (Files.exists(target)) {
        throw new IllegalStateException("Duplicate payments reporting XML file name: " + target.getFileName());
      }
      Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
      copiedFiles.add(target);
    }
    return List.copyOf(copiedFiles);
  }
}
