package it.gov.pagopa.mypay2pu.extractor.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static it.gov.pagopa.mypay2pu.extractor.utils.Constants.ZONEID;

public class TimeUtils {

  public static ZoneOffset zoneOffsetAt(LocalDateTime localDateTime) {
    return ZONEID.getRules().getOffset(localDateTime);
  }
}
