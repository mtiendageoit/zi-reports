package com.zonainmueble.reports.maps.google;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.services.MapImageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class GoogleMapsService implements MapImageService {

  @Value("${apis.google.maps.key}")
  private String key;

  @Value("${apis.google.maps.static.url}")
  private String staticUrl;

  private final RestTemplate restTemplate;

  public GoogleMapsService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public byte[] image(MapImageRequest input) {
    String url = buildUrl(input);

    try {
      ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
      return response.getBody();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("input: {}, url: {}", input, url);
      throw new RuntimeException("Failed to fetch image from google maps static");
    }
  }

  private String buildUrl(MapImageRequest input) {
    // https://maps.googleapis.com/maps/api/staticmap?format=png&maptype=roadmap&size=500x500&path=color:0x0000ff77|weight:2|fillcolor:0x0000ff33|20.067457,-98.4051|20.067403,-98.406184|20.063318,-98.407633|20.061864,-98.4051|20.062736,-98.403517|20.064318,-98.402283|20.064952,-98.402466|20.066318,-98.404391|20.067318,-98.404528|20.067457,-98.4051&markers=20.0643184,-98.4040995&key=key

    StringBuilder urlBuilder = new StringBuilder(staticUrl).append("?");
    urlBuilder.append("format=png");
    urlBuilder.append("&maptype=").append(input.getMapType());
    urlBuilder.append("&size=").append(input.getSize().getWidth()).append("x").append(input.getSize().getHeight());

    if (input.getMarkers() != null && !input.getMarkers().isEmpty()) {
      urlBuilder.append("&").append(markersToStr(input.getMarkers()));
    }
    if (input.getPolygons() != null && !input.getPolygons().isEmpty()) {
      urlBuilder.append("&").append(pathsToStr(input.getPolygons()));
    }

    urlBuilder.append("&key=").append(key);

    return urlBuilder.toString();
  }

  private String pathsToStr(List<Polygon> polygons) {
    return polygons.stream().map(poly -> pathToStr(poly)).collect(Collectors.joining("&"));
  }

  private String pathToStr(Polygon polygon) {

    String coords = polygon.getCoordinates().stream()
        .map(coord -> String.format("%f,%f", coord.getLatitude(), coord.getLongitude()))
        .collect(Collectors.joining("|"));

    return new StringBuilder("path=color:0x0000ff77|weight:2|fillcolor:0x0000ff33|")
        .append(coords).toString();
  }

  private String markersToStr(List<Marker> markers) {
    return markers.stream().map(marker -> markerToStr(marker)).collect(Collectors.joining("&"));
  }

  private String markerToStr(Marker marker) {
    Coordinate coord = marker.getCoordinate();
    return new StringBuilder("markers=")
        .append(coord.getLatitude()).append(",").append(coord.getLongitude())
        .toString();
  }

}
