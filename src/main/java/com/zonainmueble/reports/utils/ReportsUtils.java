package com.zonainmueble.reports.utils;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ReportsUtils {

  public String reportBuildTime() {
    return DateTimeUtils.format(LocalDateTime.now(), "dd/MMMM/yyyy h:mm a", new Locale("es", "ES"));
  }
}
