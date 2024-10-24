package com.zonainmueble.reports.enums;

public enum PoiCategory {
  GASOLINERA("700-7600-0116"),
  ESTACIONAMIENTO("800-8500-0178");

  private String hereMapsKey;

  private PoiCategory(String hereMapsKey) {
    this.hereMapsKey = hereMapsKey;
  }

  public String key() {
    return hereMapsKey;
  }
}
