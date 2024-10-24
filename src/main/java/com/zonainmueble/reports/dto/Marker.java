package com.zonainmueble.reports.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marker {
  private String color;
  private String label;
  private String category;
  private Double distance;
  private MarkerSize size;
  private Coordinate coordinate;

  public Marker(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public enum MarkerSize {
    tiny, mid, small, normal
  }

}
