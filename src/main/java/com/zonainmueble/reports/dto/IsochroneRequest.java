package com.zonainmueble.reports.dto;

import java.util.List;

import com.zonainmueble.reports.enums.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IsochroneRequest {
  private Coordinate center;
  private IsochroneMode mode;
  private List<Integer> modeValues; // for distance in meters, for time in minutes
  private TransportType transportType;
}
