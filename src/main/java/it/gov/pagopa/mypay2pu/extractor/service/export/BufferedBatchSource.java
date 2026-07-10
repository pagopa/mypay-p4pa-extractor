package it.gov.pagopa.mypay2pu.extractor.service.export;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Buffering adapter for suppliers that return data in batches.
 * It allows the caller to preview, consume, and restore items while preserving
 * the original ordering of the underlying source.
 *
 * @param <E> the type of items supplied by the buffered source
 */
final class BufferedBatchSource<E> {

  private final Supplier<List<E>> source;
  private final ArrayDeque<E> buffer = new ArrayDeque<>();
  private boolean exhausted;

  BufferedBatchSource(Supplier<List<E>> source) {
    this.source = source;
  }

  /**
   * Checks whether at least one item is still available from the buffer or source.
   *
   * @return {@code true} when more data can be consumed, {@code false} otherwise
   */
  boolean hasMoreData() {
    fillBufferIfNeeded();
    return !buffer.isEmpty();
  }

  /**
   * Retrieves up to the requested number of items from the buffered source.
   *
   * @param maxItems the maximum number of items to consume
   * @return the consumed items, or an empty list when no data is available
   */
  List<E> takeUpTo(int maxItems) {
    if (maxItems <= 0) {
      return List.of();
    }

    List<E> items = new ArrayList<>(maxItems);
    while (items.size() < maxItems) {
      fillBufferIfNeeded();
      if (buffer.isEmpty()) {
        break;
      }
      items.add(buffer.removeFirst());
    }
    return items;
  }

  /**
   * Pushes the provided items back to the head of the buffer, preserving their order.
   *
   * @param items the items to restore to the front of the buffer
   */
  void pushFront(List<E> items) {
    for (int i = items.size() - 1; i >= 0; i--) {
      buffer.addFirst(items.get(i));
    }
  }

  private void fillBufferIfNeeded() {
    while (buffer.isEmpty() && !exhausted) {
      List<E> next = source.get();
      if (next == null || next.isEmpty()) {
        exhausted = true;
        return;
      }
      buffer.addAll(next);
    }
  }
}
