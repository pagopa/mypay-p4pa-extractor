package it.gov.pagopa.mypay2pu.extractor.service.export;

/**
 * Result of inspecting the initial batches of an export data source.
 *
 * @param hasData whether the source contains at least one exportable row
 * @param multipart whether the source exceeds the configured maximum rows per part
 */
public record ExportBatchInspection(boolean hasData, boolean multipart) {
}
