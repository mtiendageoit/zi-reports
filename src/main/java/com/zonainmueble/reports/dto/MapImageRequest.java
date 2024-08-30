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
  private List<Marker> markers;
  private List<Polygon> polygons;

  public MapImageRequest(int width, int height, Marker... markers) {
    this.size = new Size(width, height);
    this.markers = Arrays.asList(markers);
  }

  public MapImageRequest(int width, int height, Polygon... polygons) {
    this.size = new Size(width, height);
    this.polygons = Arrays.asList(polygons);
  }

  public MapImageRequest(int width, int height, List<Marker> markers, List<Polygon> polygons) {
    this.size = new Size(width, height);
    this.markers = markers;
    this.polygons = polygons;
  }

}
