package com.zonainmueble.reports.dto;

import lombok.Data;

@Data
public class PolygonStyle {
  private String color;
  private Integer weight;
  private String fillColor;

  public PolygonStyle() {
    this.color = "#0000FF77";
    this.weight = 3;
  }

  public PolygonStyle(String color) {
    this();
    this.color = color;
  }

  public PolygonStyle(String color, int weight) {
    this(color);
    this.weight = weight;
  }

  public PolygonStyle(String color, String fillColor) {
    this(color);
    this.fillColor = fillColor;
  }

  public PolygonStyle(String color, String fillColor, int weight) {
    this(color, fillColor);
    this.weight = weight;
  }
}