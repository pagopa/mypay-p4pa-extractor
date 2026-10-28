package it.gov.pagopa.mypay2pu.extractor.utils;

import org.slf4j.MDC;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Utilities {
  private static final DateTimeFormatter XML_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private Utilities() {
  }

  private static final DatatypeFactory DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR;

  static {
    try {
      DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null
      ? DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR.newXMLGregorianCalendar(offsetDateTime.toString())
      : null;
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate date) {
    return date != null ? DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR.newXMLGregorianCalendar(date.toString()) : null;
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime dateTime) {
    return dateTime != null
      ? DATATYPE_FACTORY_XML_GREGORIAN_CALENDAR.newXMLGregorianCalendar(dateTime.format(XML_DATE_TIME_FORMATTER))
      : null;
  }

}
