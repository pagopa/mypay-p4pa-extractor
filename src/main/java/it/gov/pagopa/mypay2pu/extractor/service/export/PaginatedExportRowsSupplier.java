package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class PaginatedExportRowsSupplier<M, C> implements Supplier<List<C>> {

  private final BiFunction<Integer, Integer, List<M>> retriever;
  private final Function<M, C> mapper;
  private final int pageSize;
  private int offset;
  private boolean exhausted;

  PaginatedExportRowsSupplier(BiFunction<Integer, Integer, List<M>> retriever,
                              Function<M, C> mapper,
                              int pageSize) {
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be positive");
    }
    this.retriever = Objects.requireNonNull(retriever, "Retriever is required");
    this.mapper = Objects.requireNonNull(mapper, "Mapper is required");
    this.pageSize = pageSize;
  }

  @Override
  public List<C> get() {
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
    return models.stream().map(mapper).toList();
  }
}
