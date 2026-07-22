package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Retrieves source models from the database in paginated batches.
 *
 * @param <M> source model type
 */
final class PaginatedExportRowsSupplier<M> implements Supplier<List<M>> {

  private final BiFunction<Integer, Integer, List<M>> retriever;
  private final int pageSize;
  private int offset;
  private boolean exhausted;

  PaginatedExportRowsSupplier(BiFunction<Integer, Integer, List<M>> retriever,
                              int pageSize) {
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be positive");
    }
    this.retriever = Objects.requireNonNull(retriever, "Retriever is required");
    this.pageSize = pageSize;
  }

  @Override
  public List<M> get() {
    if (exhausted) {
      return List.of();
    }
    List<M> models = retriever.apply(pageSize, offset);
    if (CollectionUtils.isEmpty(models)) {
      exhausted = true;
      return List.of();
    }
    offset += models.size();
    if (models.size() < pageSize) {
      exhausted = true;
    }
    return models;
  }
}
