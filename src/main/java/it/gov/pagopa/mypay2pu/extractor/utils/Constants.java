package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

public class Constants {

  private Constants(){}

  public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");
  public static final ZoneOffset ZONEOFFSET = ZONEID.getRules().getOffset(Instant.now());
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZONEID);

  public static final String CSV_LOGICAL_KEY_SEPARATOR = ",";
  public static final String LOGICAL_KEY_PAIR_SEPARATOR = "|";
}

