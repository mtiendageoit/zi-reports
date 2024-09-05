package com.zonainmueble.reports.maps.here.isoline;

import java.util.List;

import lombok.Data;

@Data
public class IsolineResponse {
  private Departure departure;
  private List<Isoline> isolines;
}
