package com.zonainmueble.reports.maps.here.isoline;

import com.zonainmueble.reports.maps.here.Position;

import lombok.Data;

@Data
public class Place {
  private String type;
  private Position location;
  private Position originalLocation;
}
