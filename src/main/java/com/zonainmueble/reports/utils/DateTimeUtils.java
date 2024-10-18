package com.zonainmueble.reports.utils;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.time.*;

public class DateTimeUtils {

  public static String format(LocalDateTime date, String pattern, Locale locale) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
    return date.format(formatter);
  }

  public static String format(LocalDateTime date, String pattern) {
    Locale locale = new Locale("es", "MX");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
    return date.format(formatter);
  }

  public static LocalDateTime now() {
    ZoneId zoneId = ZoneId.of("America/Mexico_City");
    return LocalDateTime.now(zoneId);
  }

  public static LocalDateTime getPreviousFromNow(DayOfWeek dayOfWeek) {
    LocalDateTime now = now();
    return now.with(TemporalAdjusters.previous(dayOfWeek));
  }
}