package com.zonainmueble.reports.maps.here.isoline;

import java.util.List;
import lombok.Data;

@Data
public class Isoline {
  private Range range;
  private List<Polygon> polygons;
}
