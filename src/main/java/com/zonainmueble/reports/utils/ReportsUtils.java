package com.zonainmueble.reports.utils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.zonainmueble.reports.dto.Coordinate;
import com.zonainmueble.reports.dto.Extent;
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

  public static Extent extentFrom(Polygon poly) {
    Assert.notNull(poly, "The polygon must not be null");
    Assert.notEmpty(poly.getCoordinates(), "The coordinates must not be empty");

    double minLatitude = poly.getCoordinates().get(0).getLatitude();
    double maxLatitude = poly.getCoordinates().get(0).getLatitude();
    double minLongitude = poly.getCoordinates().get(0).getLongitude();
    double maxLongitude = poly.getCoordinates().get(0).getLongitude();

    for (Coordinate coordinate : poly.getCoordinates()) {
      minLatitude = Math.min(minLatitude, coordinate.getLatitude());
      maxLatitude = Math.max(maxLatitude, coordinate.getLatitude());
      minLongitude = Math.min(minLongitude, coordinate.getLongitude());
      maxLongitude = Math.max(maxLongitude, coordinate.getLongitude());
    }

    return new Extent(minLatitude, minLongitude, maxLatitude, maxLongitude);
  }

}
