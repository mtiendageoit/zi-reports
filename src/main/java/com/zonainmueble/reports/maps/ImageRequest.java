package com.zonainmueble.reports.maps;

import lombok.Data;

@Data
public class ImageRequest {
  private double longitude;
  private double latitude;

  private int width;
  private int height;

  private boolean isoVisible;
}
