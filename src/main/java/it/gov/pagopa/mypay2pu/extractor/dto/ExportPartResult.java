package it.gov.pagopa.mypay2pu.extractor.dto;

import java.util.Optional;

/**
 * Describes the outcome of writing a single export part.
 *
 * @param fileName the generated ZIP file name
 * @param errorFileName the generated error report file name, when present
 */
public record ExportPartResult(String fileName, Optional<String> errorFileName) {
}
