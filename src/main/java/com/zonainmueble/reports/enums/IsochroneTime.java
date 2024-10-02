package com.zonainmueble.reports.enums;

public enum IsochroneTime {
  FIVE_MINUTES(5), TEN_MINUTES(10), FIFTEEN_MINUTES(15);

  private final int value;

  IsochroneTime(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
