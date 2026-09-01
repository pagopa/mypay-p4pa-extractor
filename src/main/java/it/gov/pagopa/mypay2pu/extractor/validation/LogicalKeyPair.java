package it.gov.pagopa.mypay2pu.extractor.validation;

import java.util.List;

/**
 * Holds the two logical-key lists parsed from a paired logical key expression.
 *
 * @param left values before the vertical bar
 * @param right values after the vertical bar
 */
public record LogicalKeyPair(List<String> left, List<String> right) {
}
