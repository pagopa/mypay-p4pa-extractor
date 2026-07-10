package it.gov.pagopa.mypay2pu.extractor.service.export;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ExportBatchCoordinatorTest {

  private final ExportBatchCoordinator exportBatchCoordinator = new ExportBatchCoordinator();

  @Test
  void givenSingleBatchSupplierWhenCalledTwiceThenReturnsDataFirstTimeEmptySecond() {
    Supplier<List<String>> supplier = exportBatchCoordinator.createSingleBatchSupplier(
      List.of("row1", "row2")
    );

    assertEquals(List.of("row1", "row2"), supplier.get());
    assertTrue(supplier.get().isEmpty());
  }

  @Test
  void givenPagedSupplierWithBatchSizeWhenConsumingAllPagesThenUsesCorrectOffsetsUntilLastPage() {
    List<Integer> offsets = new ArrayList<>();
    Supplier<List<String>> supplier = exportBatchCoordinator.createPagedSupplier(
      2,
      offset -> {
        offsets.add(offset);
        return switch (offset) {
          case 0 -> List.of("row1", "row2");
          case 2 -> List.of("row3");
          default -> List.of();
        };
      }
    );

    assertEquals(List.of("row1", "row2"), supplier.get());
    assertEquals(List.of("row3"), supplier.get());
    assertTrue(supplier.get().isEmpty());
    assertEquals(List.of(0, 2), offsets);
  }

  @Test
  void givenBufferedSourceWhenInspectedThenPreservesPreviewedRows() {
    Iterator<List<String>> pages = List.of(
      List.of("row1", "row2"),
      List.of("row3"),
      List.<String>of()
    ).iterator();
    BufferedBatchSource<String> bufferedRows = exportBatchCoordinator.createBufferedSource(
      pages::next
    );

    ExportBatchInspection inspection = exportBatchCoordinator.inspect(bufferedRows, 2);

    assertTrue(inspection.hasData());
    assertTrue(inspection.multipart());
    assertEquals(List.of("row1", "row2", "row3"), bufferedRows.takeUpTo(3));
    assertFalse(bufferedRows.hasMoreData());
  }

  @Test
  void givenBufferedSourceWithChunkAndPartLimitWhenCreatingMappedPartSupplierThenRespectsLimitsAndMaps() {
    Iterator<List<Integer>> pages = List.of(
      List.of(1, 2),
      List.of(3, 4),
      List.<Integer>of()
    ).iterator();
    BufferedBatchSource<Integer> bufferedRows = exportBatchCoordinator.createBufferedSource(
      pages::next
    );
    Supplier<List<String>> supplier = exportBatchCoordinator.createMappedPartSupplier(
      bufferedRows,
      3,
      2,
      Object::toString
    );

    assertEquals(List.of("1", "2"), supplier.get());
    assertEquals(List.of("3"), supplier.get());
    assertTrue(supplier.get().isEmpty());
  }
}
