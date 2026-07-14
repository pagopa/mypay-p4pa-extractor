package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BufferedPageSupplierTest {

  @Test
  void givenSourceWithRemainderWhenGetThenFillPagesAndKeepRemainingRows() {
    var supplier = new BufferedPageSupplier<>(batches(
      List.of("one"),
      List.of("two", "three", "four")
    ), 3);

    assertEquals(List.of("one", "two", "three"), supplier.get());
    assertEquals(List.of("four"), supplier.get());
    assertEquals(List.of(), supplier.get());
  }

  @Test
  void givenExhaustedSourceWhenGetThenDoNotFetchAgain() {
    var sourceCalls = new AtomicInteger();
    Supplier<List<String>> source = () -> {
      sourceCalls.incrementAndGet();
      return Collections.emptyList();
    };
    var supplier = new BufferedPageSupplier<>(source, 2);

    assertEquals(List.of(), supplier.get());
    assertEquals(List.of(), supplier.get());
    assertEquals(1, sourceCalls.get());
  }

  @Test
  void givenNonPositivePageSizeWhenConstructingThenThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new BufferedPageSupplier<>(Collections::<String>emptyList, 0));
  }

  @SafeVarargs
  private final Supplier<List<String>> batches(List<String>... batches) {
    var batchIterator = List.of(batches).iterator();
    return () -> batchIterator.hasNext() ? batchIterator.next() : Collections.emptyList();
  }
}
