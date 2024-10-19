package com.zonainmueble.reports.models;

import lombok.*;

@Data
public class PoisCategory {
  private String key;
  private String name;
  private Integer count;
  private String icon;

  public PoisCategory() {
    this.icon = "icon-pois-generic.svg";
  }

  public PoisCategory(String key, String name) {
    this();
    this.key = key;
    this.name = name;
  }

  public PoisCategory(String key, String name, String icon) {
    this(key, name);
    this.icon = icon;
  }
}
