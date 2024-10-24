package com.zonainmueble.reports.dto;

import com.zonainmueble.reports.enums.*;

import lombok.*;

@Data
@Builder
public class Isochrone {
  private IsochroneMode mode;
  private Integer modeValue;
  private TransportType transportType;

  private Polygon polygon;
  private Coordinate center;
}
