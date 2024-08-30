package com.zonainmueble.reports.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class IsochroneResponse {
  private List<Polygon> polygons;
}
