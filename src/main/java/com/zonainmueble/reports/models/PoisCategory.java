package com.zonainmueble.reports.models;

import lombok.*;

@Data
public class PoisCategory {
  public static final String DEFAULT_ICON = "icon-pois-generic.svg";
  public static final String DEFAULT_COLOR = "#000000";
  private String key;
  private String name;
  private Integer count;
  private String icon;
  private String color;

  public PoisCategory() {
    this.icon = DEFAULT_ICON;
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

  public PoisCategory(String key, String name, String icon, String color) {
    this(key, name, icon);
    this.color = color;
  }
}
