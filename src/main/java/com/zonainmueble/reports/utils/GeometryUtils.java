package com.zonainmueble.reports.utils;

import java.util.*;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeometryUtils {
  private static final double METERS_PER_DEGREE_LATITUDE = 111320.0; // Aproximadamente 111.32 km

  private final GeometryFactory geometryFactory;

  private GeometryUtils() {
    this.geometryFactory = new GeometryFactory();
  }

  public com.zonainmueble.reports.dto.Extent boundingBox(com.zonainmueble.reports.dto.Polygon polygon) {
    Polygon poly = fromPolygon(polygon);
    Polygon envelope = (Polygon) poly.getEnvelope();
    double xmin = envelope.getCoordinates()[0].x;
    double ymin = envelope.getCoordinates()[0].y;

    double xmax = envelope.getCoordinates()[2].x;
    double ymax = envelope.getCoordinates()[2].y;
    return new com.zonainmueble.reports.dto.Extent(ymin, xmin, ymax, xmax);
  }

  public com.zonainmueble.reports.dto.Polygon buffer(com.zonainmueble.reports.dto.Polygon polygon, double distance) {
    Polygon poly = fromPolygon(polygon);
    return toPolygon((Polygon) buffer(poly, distance));
  }

  public String polygonToWKT(com.zonainmueble.reports.dto.Polygon polygon) {
    return toWKT(fromPolygon(polygon));
  }

  private Polygon fromPolygon(com.zonainmueble.reports.dto.Polygon polygon) {
    List<Point> points = polygon.getCoordinates().stream()
        .map(coord -> point(coord.getLatitude(), coord.getLongitude())).collect(Collectors.toList());
    return toPolygon(points);
  }

  private com.zonainmueble.reports.dto.Polygon toPolygon(Polygon polygon) {
    List<com.zonainmueble.reports.dto.Coordinate> coords = new ArrayList<>();

    for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
      coords.add(new com.zonainmueble.reports.dto.Coordinate(coord.getY(), coord.getX()));
    }

    return new com.zonainmueble.reports.dto.Polygon(coords);
  }

  // private Optional<Geometry> fromWKT(String wkt) {
  // WKTReader reader = new WKTReader(geometryFactory);
  // try {
  // return Optional.of(reader.read(wkt));
  // } catch (ParseException e) {
  // log.error(e.getMessage(), e);
  // log.error("Failed to get Geometry from wkt: {}", wkt);
  // }
  // return Optional.empty();
  // }

  private Geometry buffer(Geometry geometry, double distanceMeters) {
    double distance = distanceMeters / METERS_PER_DEGREE_LATITUDE;
    Geometry buffer = geometry.buffer(distance);
    Geometry simplify = TopologyPreservingSimplifier.simplify(buffer, 0.0000488);
    return simplify;
  }

  private String toWKT(Geometry geometry) {
    return geometry.toText();
  }

  private Point point(double latitude, double longitude) {
    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }

  private boolean coversPoint(Geometry geometry, Point point) {
    return geometry.covers(point);
  }

  private Polygon toPolygon(List<Point> points) {
    List<Coordinate> coords = points.stream().map(point -> point.getCoordinate()).collect(Collectors.toList());
    return geometryFactory.createPolygon(coords.toArray(new Coordinate[0]));
  }

  public List<com.zonainmueble.reports.maps.here.pois.Poi> within(
      List<com.zonainmueble.reports.maps.here.pois.Poi> pois, com.zonainmueble.reports.dto.Polygon isochrone) {
    Polygon poly = fromPolygon(isochrone);

    List<com.zonainmueble.reports.maps.here.pois.Poi> result = new ArrayList<>();

    for (com.zonainmueble.reports.maps.here.pois.Poi poi : pois) {
      boolean isWithin = coversPoint(poly, point(poi.getPosition().getLat(), poi.getPosition().getLng()));
      if (isWithin) {
        result.add(poi);
      }
    }
    return result;
  }
}
