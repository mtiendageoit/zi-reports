package com.zonainmueble.reports.dto;

import lombok.Data;

@Data
public class PolygonStyle {
  private String color;
  private Integer weight;
  private String fillColor;

  public PolygonStyle() {
    this.color = "#0000FF77";
    this.weight = 2;
    this.fillColor = "#0000FF33";
  }

  public PolygonStyle(String color, String fillColor) {
    this();
    this.color = color;
    this.fillColor = fillColor;
  }

  public PolygonStyle(String color, String fillColor, int weight) {
    this(color, fillColor);
    this.weight = weight;
  }
}