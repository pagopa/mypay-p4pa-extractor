package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Coordinates batching concerns for exports.
 * It provides reusable strategies for single-batch sources, paged sources,
 * buffered previews, multipart inspection, and row mapping per generated file part.
 */
@Component
public class ExportBatchCoordinator {

  /**
   * Wraps a preloaded data set into a supplier that can be consumed only once.
   *
   * @param data the preloaded data to expose as a batch source
   * @param <T> the type of items in the batch
   * @return a supplier that returns the provided data on first invocation and an empty list afterwards
   */
  public <T> Supplier<List<T>> createSingleBatchSupplier(List<T> data) {
    return new Supplier<>() {
      private boolean consumed;

      @Override
      public List<T> get() {
        if (consumed) {
          return List.of();
        }
        consumed = true;
        return data;
      }
    };
  }

  /**
   * Creates a paging supplier that keeps track of the current offset and marks
   * the source as exhausted when a page smaller than the requested size is returned.
   *
   * @param pageSize the requested page size
   * @param pageRetriever callback used to load a page starting from the current offset
   * @param <T> the type of items returned by the page retriever
   * @return a supplier that iterates over the paged source
   */
  public <T> Supplier<List<T>> createPagedSupplier(
    int pageSize,
    IntFunction<List<T>> pageRetriever
  ) {
    int effectivePageSize = Math.max(1, pageSize);
    AtomicInteger offset = new AtomicInteger(0);
    AtomicBoolean exhausted = new AtomicBoolean(false);

    return () -> {
      if (exhausted.get()) {
        return List.of();
      }

      List<T> page = pageRetriever.apply(offset.get());
      offset.addAndGet(page.size());
      if (page.size() < effectivePageSize) {
        exhausted.set(true);
      }
      return page;
    };
  }

  /**
   * Wraps a batch supplier into a buffering source that supports preview and replay.
   *
   * @param source the underlying batch supplier
   * @param <T> the type of items produced by the supplier
   * @return a buffered view over the supplied batches
   */
  public <T> BufferedBatchSource<T> createBufferedSource(Supplier<List<T>> source) {
    return new BufferedBatchSource<>(source);
  }

  /**
   * Inspects the beginning of the buffered source to determine whether data exists
   * and whether multipart generation is required, then restores the previewed rows.
   *
   * @param bufferedRows the buffered source to inspect
   * @param maxRowsPerPart the maximum number of rows allowed in a single export part
   * @param <T> the type of items contained in the buffered source
   * @return the inspection result describing data presence and multipart need
   */
  public <T> ExportBatchInspection inspect(
    BufferedBatchSource<T> bufferedRows,
    int maxRowsPerPart
  ) {
    List<T> previewRows = bufferedRows.takeUpTo(maxRowsPerPart + 1);
    boolean hasData = !previewRows.isEmpty();
    boolean multipart = previewRows.size() > maxRowsPerPart;
    bufferedRows.pushFront(previewRows);
    return new ExportBatchInspection(hasData, multipart);
  }

  /**
   * Creates a supplier that consumes at most one export part from the buffered source
   * and maps raw rows into CSV DTOs in chunks.
   *
   * @param bufferedRows the buffered source containing raw export rows
   * @param maxRowsPerPart the maximum number of rows to include in the current part
   * @param chunkSize the maximum number of raw rows to consume per supplier invocation
   * @param mapper the transformation applied to each raw row
   * @param <T> the type of raw rows
   * @param <D> the type of mapped CSV rows
   * @return a supplier that emits mapped rows for a single export part
   */
  public <T, D> Supplier<List<D>> createMappedPartSupplier(
    BufferedBatchSource<T> bufferedRows,
    int maxRowsPerPart,
    int chunkSize,
    Function<T, D> mapper
  ) {
    int effectiveChunkSize = Math.max(1, chunkSize);
    return new Supplier<>() {
      private int remaining = maxRowsPerPart;

      @Override
      public List<D> get() {
        if (remaining <= 0) {
          return List.of();
        }

        List<T> rawRows = bufferedRows.takeUpTo(Math.min(effectiveChunkSize, remaining));
        if (rawRows.isEmpty()) {
          return List.of();
        }

        remaining -= rawRows.size();
        return rawRows.stream()
          .map(mapper)
          .toList();
      }
    };
  }
}
