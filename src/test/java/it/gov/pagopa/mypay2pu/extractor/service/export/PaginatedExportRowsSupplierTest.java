package it.gov.pagopa.mypay2pu.extractor.service.export;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginatedExportRowsSupplierTest {

  @Test
  void whenSourcePageIsShortThenDoNotQueryAgain() {
    AtomicInteger calls = new AtomicInteger();
    TestExportModel first = new TestExportModel("a");
    TestExportModel second = new TestExportModel("b");
    TestExportModel third = new TestExportModel("c");
    BiFunction<Integer, Integer, List<TestExportModel>> retriever = (limit, offset) -> {
      calls.incrementAndGet();
      return switch (calls.get()) {
        case 1 -> List.of(first, second);
        case 2 -> List.of(third);
        default -> throw new IllegalStateException("Unexpected extra query");
      };
    };

    PaginatedExportRowsSupplier<TestExportModel> supplier =
      new PaginatedExportRowsSupplier<>(retriever, 2);

    assertEquals(List.of(first, second), supplier.get());
    assertEquals(List.of(third), supplier.get());
    assertEquals(List.of(), supplier.get());
    assertEquals(2, calls.get());
  }

  @Test
  void whenPageSizeIsNotPositiveThenRejectIt() {
    assertThrows(IllegalArgumentException.class,
      () -> new PaginatedExportRowsSupplier<>((limit, offset) -> List.of(), 0));
  }

  private record TestExportModel(String value) implements ExportModel {

    @Override
    public String logicalKey() {
      return value;
    }
  }
}
