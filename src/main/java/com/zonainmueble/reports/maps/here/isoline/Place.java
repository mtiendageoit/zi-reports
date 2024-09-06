package com.zonainmueble.reports.maps.here.isoline;

import com.zonainmueble.reports.maps.here.Location;

import lombok.Data;

@Data
public class Place {
  private String type;
  private Location location;
  private Location originalLocation;
}
