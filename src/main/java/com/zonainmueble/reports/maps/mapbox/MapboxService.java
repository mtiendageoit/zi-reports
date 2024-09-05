package com.zonainmueble.reports.maps.mapbox;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.services.IsochroneService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MapboxService implements IsochroneService {
  @Value("${apis.mapbox.key}")
  private String accessToken;

  @Value("${apis.mapbox.isochrone.url}")
  private String isochroneUrl;

  private final RestTemplate restTemplate;

  public MapboxService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public IsochroneResponse isochroneFrom(IsochroneRequest input) {
    String url = buildUrl(input);

    try {
      ResponseEntity<FeatureCollection> response = restTemplate.getForEntity(url, FeatureCollection.class);
      return buildResponse(response.getBody());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("input: {}, url: {}", input, url);
      throw new RuntimeException("Failed to fetch isochrone from mapbox");
    }
  }

  private IsochroneResponse buildResponse(FeatureCollection collection) {

    if (collection == null) {
      throw new RuntimeException("No FeatureCollection in response");
    }

    List<Polygon> isochrones = new ArrayList<>();
    for (Feature item : collection.getFeatures()) {
      isochrones.add(from(item));
    }

    return new IsochroneResponse(isochrones);
  }

  private Polygon from(Feature feature) {
    Polygon iso = new Polygon();
    for (List<Double> lonlat : feature.getGeometry().getCoordinates().get(0)) {
      iso.getCoordinates().add(new Coordinate(lonlat.get(1), lonlat.get(0)));
    }
    return iso;
  }

  private String buildUrl(IsochroneRequest request) {
    // https://api.mapbox.com/isochrone/v1/mapbox/walking/-99.1690065,19.3701724?contours_minutes=5,10,15&polygons=true&denoise=1&generalize=0&access_token=token

    String mode = isochroneModeFrom(request.getMode());
    String type = isochroneTypeFrom(request.getTransportType());
    String contours = StringUtils.collectionToCommaDelimitedString(request.getModeValues());
    String coordinates = String.format("%f,%f", request.getCenter().getLongitude(), request.getCenter().getLatitude());

    StringBuilder urlBuilder = new StringBuilder(isochroneUrl);
    urlBuilder.append("/").append(type);
    urlBuilder.append("/").append(coordinates);
    urlBuilder.append("?").append(mode).append("=").append(contours);
    urlBuilder.append("&polygons=true");
    urlBuilder.append("&denoise=1");
    urlBuilder.append("&access_token").append("=").append(accessToken);

    return urlBuilder.toString();
  }

  private String isochroneTypeFrom(TransportType type) {
    switch (type) {
      case WALKING:
        return "walking";
      case CYCLING:
        return "cycling";
      case DRIVING:
        return "driving";
      case DRIVING_TRAFFIC:
        return "driving-traffic";
      default:
        throw new NoSuchElementException("Element not exists");
    }
  }

  private String isochroneModeFrom(IsochroneMode mode) {
    switch (mode) {
      case TIME:
        return "contours_minutes";
      case DISTANCE:
        return "contours_meters";
      default:
        throw new NoSuchElementException("Element not exists");
    }
  }

}
