package com.zonainmueble.reports.dto;

import java.util.List;
import java.time.LocalDateTime;

import com.zonainmueble.reports.enums.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IsochroneRequest {
  private Integer maxPoints;
  private Coordinate center;
  private IsochroneMode mode;
  private List<Integer> modeValues;
  private TransportType transportType;
  private LocalDateTime departureTime;
}
