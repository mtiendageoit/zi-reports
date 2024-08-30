package com.zonainmueble.reports.maps.mapbox;

import lombok.Data;
import java.util.List;

@Data
public class FeatureCollection {
  private List<Feature> features;
}