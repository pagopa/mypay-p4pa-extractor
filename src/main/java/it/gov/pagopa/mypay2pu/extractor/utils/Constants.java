package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

public class Constants {

  private Constants(){}

  public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZONEID);

  public static final String LOGICAL_KEY_VALUE_SEPARATOR = ",";
  public static final String LOGICAL_KEY_PAIR_SEPARATOR = "|";

  public static ZoneOffset zoneOffsetAt(Instant instant) {
    return ZONEID.getRules().getOffset(instant);
  }

  public static ZoneOffset zoneOffsetAt(LocalDateTime localDateTime) {
    return ZONEID.getRules().getOffset(localDateTime);
  }
}
