package com.zonainmueble.reports.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryData {
  private String category;
  private Double value;
  private String color;
}
