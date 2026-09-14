package it.gov.pagopa.mypay2pu.extractor.service.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Produces export artifacts from a paginated sequence of items.
 *
 * <p>Implementations define the output format while preserving the common
 * streaming contract: callers provide one page at a time and receive the
 * materialized files in deterministic order.</p>
 *
 * @param <C> item type accepted by the writer
 */
public interface PartitionWriterService<C> {

  /**
   * Writes all items supplied in consecutive pages.
   *
   * @param workingDirectory directory used for temporary artifacts
   * @param fileNameBuilder builder for output names
   * @param itemClass runtime type of the supplied items
   * @param sourceSupplier supplier returning the next page, or an empty list when exhausted
   * @param outputProfile format-specific output profile
   * @param maxItemsPerPart maximum number of items in one partition
   * @return generated artifact paths
   * @throws IOException if an artifact cannot be written
   * @param <T> concrete item type
   */
  <T extends C> List<Path> writePartitions(Path workingDirectory,
                                           ExportFileNameBuilder fileNameBuilder,
                                           Class<T> itemClass,
                                           Supplier<? extends List<? extends T>> sourceSupplier,
                                           String outputProfile,
                                           int maxItemsPerPart) throws IOException;
}
