package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringUtilsTest {

  @Test
  void characterToStringShouldConvertCharacterAndPreserveNull() {
    assertEquals("A", StringUtils.characterToString('A'));
    assertNull(StringUtils.characterToString(null));
  }

  @Test
  void toUtf8StringShouldConvertBytesAndPreserveNull() {
    assertEquals("Pàgopa", StringUtils.toUtf8String("Pàgopa".getBytes(StandardCharsets.UTF_8)));
    assertNull(StringUtils.toUtf8String(null));
  }
}
