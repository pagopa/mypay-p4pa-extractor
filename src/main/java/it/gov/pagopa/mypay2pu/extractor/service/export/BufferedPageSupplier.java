package it.gov.pagopa.mypay2pu.extractor.service.export;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A decorator supplier that buffers source batches and returns fixed-size pages.
 *
 * @param <T> the source row type
 */
public class BufferedPageSupplier<T> implements Supplier<List<T>> {

  private final Supplier<List<T>> source;
  private final int pageSize;
  private final Deque<T> buffer = new ArrayDeque<>();
  private boolean sourceExhausted;

  /**
   * Constructs a supplier that returns pages of the requested size.
   *
   * @param source the batch supplier; it must return an empty or {@code null} batch when exhausted
   * @param pageSize the number of rows in each non-final page
   */
  public BufferedPageSupplier(Supplier<List<T>> source, int pageSize) {
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be positive");
    }
    this.source = Objects.requireNonNull(source, "Source supplier is required");
    this.pageSize = pageSize;
  }

  /**
   * Retrieves the next full page, unless the source has been exhausted.
   *
   * @return a full page, the final partial page, or an empty list when exhausted
   */
  @Override
  public List<T> get() {
    List<T> page = new ArrayList<>(pageSize);
    while (page.size() < pageSize) {
      drainBuffer(page);
      if (page.size() == pageSize || sourceExhausted) {
        break;
      }

      List<T> rows = source.get();
      if (rows == null || rows.isEmpty()) {
        sourceExhausted = true;
      } else {
        buffer.addAll(rows);
      }
    }
    return page;
  }

  private void drainBuffer(List<T> page) {
    while (page.size() < pageSize && !buffer.isEmpty()) {
      page.add(buffer.removeFirst());
    }
  }
}
