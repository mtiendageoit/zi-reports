package com.zonainmueble.reports.dto;

import java.util.Arrays;
import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapImageRequest {
  private Size size;
  private String mapType;
  private List<Marker> markers;
  private List<Polygon> polygons;

  public MapImageRequest(int width, int height, String mapType, Marker... markers) {
    this.mapType = mapType;
    this.size = new Size(width, height);
    this.markers = Arrays.asList(markers);
  }

  public MapImageRequest(int width, int height, String mapType, Polygon... polygons) {
    this.mapType = mapType;
    this.size = new Size(width, height);
    this.polygons = Arrays.asList(polygons);
  }

  public MapImageRequest(int width, int height, String mapType, List<Marker> markers, List<Polygon> polygons) {
    this.mapType = mapType;
    this.markers = markers;
    this.polygons = polygons;
    this.size = new Size(width, height);
  }

}
