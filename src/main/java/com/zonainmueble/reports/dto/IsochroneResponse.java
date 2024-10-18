package com.zonainmueble.reports.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsochroneResponse {
  private List<Isochrone> isochrones;
}
