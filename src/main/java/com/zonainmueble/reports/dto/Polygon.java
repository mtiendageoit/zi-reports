package com.zonainmueble.reports.dto;

import java.util.*;

import lombok.*;

@Data
public class Polygon {
  private PolygonStyle style;
  private List<Coordinate> coordinates;

  public Polygon() {
    this.style= new PolygonStyle();
    this.coordinates = new ArrayList<>();
  }

  public Polygon(List<Coordinate> coordinates) {
    this();
    this.coordinates = coordinates;
  }
}
