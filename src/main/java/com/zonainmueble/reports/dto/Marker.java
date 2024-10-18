package com.zonainmueble.reports.dto;

import lombok.*;

@Data
public class Marker {
  private String color;
  private String label;
  private MarkerSize size;
  private Coordinate coordinate;

  public Marker(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public Marker(Coordinate coordinate, String color) {
    this(coordinate);
    this.color = color;
  }

  public Marker(Coordinate coordinate, String color, MarkerSize size) {
    this(coordinate, color);
    this.size = size;
  }

  public Marker(Coordinate coordinate, String color, MarkerSize size, String label) {
    this(coordinate, color, size);
    this.label = label;
  }

  public enum MarkerSize {
    tiny, mid, small
  }

}
