package com.zonainmueble.reports.utils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.zonainmueble.reports.dto.Polygon;

public class ReportsUtils {
  public static String reportBuildTime() {
    return DateTimeUtils.format(LocalDateTime.now(), "dd/MMMM/yyyy h:mm a", new Locale("es", "ES"));
  }

  public static String polygonToWKT(Polygon polygon) {
    Assert.notNull(polygon, "The polygon must not be null");
    Assert.notEmpty(polygon.getCoordinates(), "The coordinates must not be empty");

    String coords = polygon.getCoordinates().stream()
        .map((coord) -> coord.getLongitude() + " " + coord.getLatitude())
        .collect(Collectors.joining(",")).toString();

    return "POLYGON((" + coords + "))";
  }

}
