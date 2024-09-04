package com.zonainmueble.reports.dto;

import com.zonainmueble.reports.enums.PoiType;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poi {
  private int total;
  private PoiType type;
}
