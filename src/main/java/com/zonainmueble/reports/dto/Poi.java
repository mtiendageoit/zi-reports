package com.zonainmueble.reports.dto;

import com.zonainmueble.reports.enums.PoiCategory;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poi {
  private int total;
  private PoiCategory type;
}
