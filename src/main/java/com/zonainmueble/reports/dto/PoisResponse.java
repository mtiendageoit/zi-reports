package com.zonainmueble.reports.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoisResponse {
  List<Poi> pois;
}
