package it.gov.pagopa.mypay2pu.extractor.utils;

import java.nio.charset.StandardCharsets;

public final class StringUtils {

  private StringUtils() {
  }

  public static String characterToString(Character value) {
    return value == null ? null : value.toString();
  }

  public static String toUtf8String(byte[] value) {
    return value == null ? null : new String(value, StandardCharsets.UTF_8);
  }
}
