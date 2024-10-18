package com.zonainmueble.reports.enums;

public enum IsochroneTime {
  FIVE_MINUTES(5), TEN_MINUTES(10), FIFTEEN_MINUTES(15), TWENTY_MINUTES(20), THIRTY_MINUTES(30), SIXTY_MINUTES(60);

  private final int value;

  IsochroneTime(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
