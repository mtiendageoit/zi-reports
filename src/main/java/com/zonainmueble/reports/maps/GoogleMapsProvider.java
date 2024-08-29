package com.zonainmueble.reports.maps;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleMapsProvider {
  private static final String BASE_URL = "https://maps.googleapis.com/maps/api/staticmap";
  private final RestTemplate restTemplate;

  @Value("${apis.google.maps.key}")
  private String googleMapsKey;

  public GoogleMapsProvider(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public byte[] image(ImageRequest input, FeatureCollection iso) {
    String staticMapUrl = buildStaticMapUrl(mapParams(input, iso));
    ResponseEntity<byte[]> response = restTemplate.getForEntity(staticMapUrl, byte[].class);
    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    } else {
      throw new RuntimeException("Failed to fetch image from Google Maps API");
    }
  }

  private Map<String, String> mapParams(ImageRequest input, FeatureCollection iso) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("format", "png");
    params.put("maptype", "roadmap");
    params.put("size", input.getWidth() + "x" + input.getHeight());
    // params.put("path",
    // "color:0x0000ff77|weight:2|fillcolor:0x0000ff33|20.067457,-98.4051|20.067403,-98.406184|20.063318,-98.407633|20.061864,-98.4051|20.062736,-98.403517|20.064318,-98.402283|20.064952,-98.402466|20.066318,-98.404391|20.067318,-98.404528|20.067457,-98.4051");
    // params.put("markers", "20.0643184,-98.4040995");

    if(input.isIsoVisible()){
      List<List<Double>> coords = iso.getFeatures().get(0).getGeometry().getCoordinates().get(0);
      String strCoors = convertCoordinatesToString(coords);
      params.put("path",
          "color:0x0000ff77|weight:2|fillcolor:0x0000ff33|" + strCoors);
    }

    params.put("markers", input.getLatitude() + "," + input.getLongitude());

    return params;
  }

  public String convertCoordinatesToString(List<List<Double>> coordinates) {
    return coordinates.stream()
        .map(coord -> String.format("%.6f,%.6f", coord.get(1), coord.get(0)))
        .collect(Collectors.joining("|"));
  }

  private String buildStaticMapUrl(Map<String, String> parameters) {
    StringBuilder urlBuilder = new StringBuilder(BASE_URL);
    urlBuilder.append("?");

    parameters.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));

    urlBuilder.append("key=").append(googleMapsKey);
    return urlBuilder.toString();
  }
}
