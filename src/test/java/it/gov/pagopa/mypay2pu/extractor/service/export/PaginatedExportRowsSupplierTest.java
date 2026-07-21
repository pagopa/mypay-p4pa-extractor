package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginatedExportRowsSupplierTest {

  @Test
  void whenSourcePageIsShortThenDoNotQueryAgain() {
    AtomicInteger calls = new AtomicInteger();
    BiFunction<Integer, Integer, List<String>> retriever = (limit, offset) -> {
      calls.incrementAndGet();
      return switch (calls.get()) {
        case 1 -> List.of("a", "b");
        case 2 -> List.of("c");
        default -> throw new IllegalStateException("Unexpected extra query");
      };
    };

    PaginatedExportRowsSupplier<String, String> supplier =
      new PaginatedExportRowsSupplier<>(retriever, Function.identity(), 2);

    assertEquals(List.of("a", "b"), supplier.get());
    assertEquals(List.of("c"), supplier.get());
    assertEquals(List.of(), supplier.get());
    assertEquals(2, calls.get());
  }

  @Test
  void whenPageSizeIsNotPositiveThenRejectIt() {
    Function<Object, Object> identity = Function.identity();
    assertThrows(IllegalArgumentException.class,
      () -> {
        new PaginatedExportRowsSupplier<>( (limit, offset) -> List.of(), identity, 0);
      });
  }
}

