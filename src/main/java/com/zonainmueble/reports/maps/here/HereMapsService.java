package com.zonainmueble.reports.maps.here;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.maps.here.PolylineEncoderDecoder.LatLngZ;
import com.zonainmueble.reports.maps.here.isoline.Isoline;
import com.zonainmueble.reports.maps.here.isoline.IsolineResponse;
import com.zonainmueble.reports.maps.here.pois.HereMapsPoisResponse;
import com.zonainmueble.reports.maps.here.pois.Poi;
import com.zonainmueble.reports.services.IsochroneService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class HereMapsService implements IsochroneService {
  @Value("${apis.here.maps.key}")
  private String key;

  @Value("${apis.here.maps.browse.url}")
  private String browseUrl;

  @Value("${apis.here.maps.isoline.url}")
  private String isolineUrl;

  private final RestTemplate restTemplate;

  public HereMapsService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public HereMapsPoisResponse pois(PoisRequest input) {
    String url = buildPoisUrl(input);

    try {
      ResponseEntity<HereMapsPoisResponse> response = restTemplate.getForEntity(url, HereMapsPoisResponse.class);

      List<Poi> pois = response.getBody().getItems().stream()
          .filter(item -> item.getMainCategory() != null && item.getResultType().equalsIgnoreCase("place"))
          .collect(Collectors.toList());
      response.getBody().setItems(pois);

      return response.getBody();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("url: {}, input: {}", url, input);
      throw new RuntimeException("Failed to fetch pois from HereMaps");
    }
  }

  private String buildPoisUrl(PoisRequest input) {
    StringBuilder urlBuilder = new StringBuilder(browseUrl).append("?");
    urlBuilder.append("at=").append(input.getCenter().getLatitude()).append(",")
        .append(input.getCenter().getLongitude());

    if (!CollectionUtils.isEmpty(input.getCategories())) {
      String categories = String.join(",", input.getCategories());
      urlBuilder.append("&categories=").append(categories);
    }

    if (input.getBoundingBox() != null) {
      Extent bb = input.getBoundingBox();

      StringJoiner join = new StringJoiner(",");
      join.add(String.valueOf(bb.getSouthWest().getLongitude()));
      join.add(String.valueOf(bb.getSouthWest().getLatitude()));
      join.add(String.valueOf(bb.getNorthEast().getLongitude()));
      join.add(String.valueOf(bb.getNorthEast().getLatitude()));

      urlBuilder.append("&in=bbox:").append(join.toString());
    }

    if (input.getLimit() != null) {
      urlBuilder.append("&limit=").append(input.getLimit());
    }

    urlBuilder.append("&apiKey=").append(key);

    return urlBuilder.toString();
  }

  @Override
  public IsochroneResponse isochroneFrom(IsochroneRequest input) {
    String url = buildIsolineUrl(input);

    try {
      ResponseEntity<IsolineResponse> response = restTemplate.getForEntity(url, IsolineResponse.class);
      return buildIsochroneResponse(response.getBody());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("url: {}, input: {}", url, input);
      throw new RuntimeException("Failed to fetch isoline from HereMaps");
    }
  }

  private IsochroneResponse buildIsochroneResponse(IsolineResponse input) {
    List<Polygon> polygons = new ArrayList<>();
    for (Isoline isoline : input.getIsolines()) {
      polygons.add(polygonFrom(isoline));
    }
    return new IsochroneResponse(polygons);
  }

  private Polygon polygonFrom(Isoline isoline) {
    List<Coordinate> coords = new ArrayList<>();

    List<LatLngZ> locations = PolylineEncoderDecoder.decode(isoline.getPolygons().get(0).getOuter());
    for (LatLngZ loc : locations) {
      coords.add(new Coordinate(loc.lat, loc.lng));
    }
    return new Polygon(coords);
  }

  private String buildIsolineUrl(IsochroneRequest input) {
    // https://isoline.router.hereapi.com/v8/isolines?transportMode=car&origin=52.51578,13.37749&range[type]=time&range[values]=300

    String origin = String.format("%f,%f", input.getCenter().getLatitude(), input.getCenter().getLongitude());

    List<Integer> valuesList = input.getModeValues();
    if (input.getMode() == IsochroneMode.TIME) {
      int secondsInMinute = 60;
      valuesList = input.getModeValues().stream().map(item -> item * secondsInMinute).collect(Collectors.toList());
    }

    String values = StringUtils.collectionToCommaDelimitedString(valuesList);

    StringBuilder urlBuilder = new StringBuilder(isolineUrl);
    urlBuilder.append("?transportMode=").append(isochroneTypeFrom(input.getTransportType()));
    urlBuilder.append("&origin=").append(origin);
    urlBuilder.append("&range[type]=").append(isochroneModeFrom(input.getMode()));
    urlBuilder.append("&range[values]=").append(values);
    urlBuilder.append("&optimizeFor=quality");
    urlBuilder.append("&apiKey=").append(key);

    return urlBuilder.toString();
  }

  private String isochroneTypeFrom(TransportType type) {
    switch (type) {
      case WALKING:
        return "pedestrian";
      case CYCLING:
        return "bicycle";
      case DRIVING:
      case DRIVING_TRAFFIC:
        return "car";
      default:
        throw new NoSuchElementException("Element not exists");
    }
  }

  private String isochroneModeFrom(IsochroneMode mode) {
    switch (mode) {
      case TIME:
        return "time";
      case DISTANCE:
        return "distance";
      default:
        throw new NoSuchElementException("Element not exists");
    }
  }
}