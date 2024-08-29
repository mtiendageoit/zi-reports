package com.zonainmueble.reports.maps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MapboxMapProvider {
  private static final String BASE_URL = "https://api.mapbox.com/isochrone/v1/mapbox/walking";
  private final RestTemplate restTemplate;

  @Value("${apis.mapbox.key}")
  private String mapboxKey;

  public MapboxMapProvider(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public FeatureCollection isocrone(double latitude, double longitude, int minutes) {
    String queryString = String.format("/%f,%f?contours_minutes=%d&denoise=1&polygons=true&access_token=%s",
        longitude, latitude, minutes, mapboxKey);

    String url = BASE_URL + queryString;

    ResponseEntity<FeatureCollection> response = restTemplate.getForEntity(url, FeatureCollection.class);
    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    } else {
      throw new RuntimeException("Failed to fetch image from MapBox API");
    }
  }
}
