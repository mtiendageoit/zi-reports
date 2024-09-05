package com.zonainmueble.reports.models;

import lombok.*;

@Data
@AllArgsConstructor
public class PoisCategory {
  private String key;
  private String name;
  private Integer count;
  private String icon;

  public PoisCategory(String key, String name) {
    this.key = key;
    this.name = name;
  }

  public PoisCategory(String key, String name, String icon) {
    this(key, name);
    this.icon = icon;
  }
}
