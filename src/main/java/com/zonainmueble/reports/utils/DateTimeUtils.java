package com.zonainmueble.reports.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeUtils {
  public static String format(LocalDateTime date, String pattern, Locale locale) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
    return date.format(formatter);
  }
}
