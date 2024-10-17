package com.zonainmueble.reports.utils;

import java.text.DecimalFormat;

public class NumberUtils {

  public static String formatToInt(Object number) {
    return new DecimalFormat("#,###").format(number);
  }

  public static String formatToDecimal(Object number) {
    return new DecimalFormat("#,##0.0").format(number);
  }

  public static String formatToDecimal(Object number, int decimals) {
    return new DecimalFormat("#,##0." + "0".repeat(decimals)).format(number);
  }
}
