package com.zonainmueble.reports.dto;

import java.util.*;

import lombok.*;

@Data
@AllArgsConstructor
public class Polygon {
  private List<Coordinate> coordinates;

  public Polygon() {
    this.coordinates = new ArrayList<>();
  }
}
