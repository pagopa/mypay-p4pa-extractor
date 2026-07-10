package it.gov.pagopa.mypay2pu.extractor.service.export;

import java.util.Optional;

/**
 * Result of writing a single export part.
 *
 * @param fileName the generated ZIP file name
 * @param errorFileName the optional validation error file name generated alongside the ZIP
 */
public record ExportPartResult(String fileName, Optional<String> errorFileName) {
}
