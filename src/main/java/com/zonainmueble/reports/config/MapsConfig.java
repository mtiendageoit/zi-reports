package com.zonainmueble.reports.config;

import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.maps.google.GoogleMapsService;
import com.zonainmueble.reports.maps.here.HereMapsService;
import com.zonainmueble.reports.services.*;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class MapsConfig {
  private final RestTemplate restTemplate;

  @Bean
  public IsochroneService isochroneService() {
    return new HereMapsService(restTemplate);
  }

  @Bean
  public MapImageService mapImageService() {
    return new GoogleMapsService(restTemplate);
  }
}
