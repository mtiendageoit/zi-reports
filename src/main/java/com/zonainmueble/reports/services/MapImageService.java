package com.zonainmueble.reports.services;

import com.zonainmueble.reports.dto.MapImageRequest;

public interface MapImageService {
  byte[] image(MapImageRequest input);
}
