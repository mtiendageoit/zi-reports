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
import com.zonainmueble.reports.utils.DateTimeUtils;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class HereMapsService implements IsochroneService {
  private final int SECONDS_IN_MINUTE = 60;

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

  @Retry(name = "hereMapsPoisRequest", fallbackMethod = "poisFallback")
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

  public HereMapsPoisResponse poisFallback(PoisRequest input, RuntimeException e) {
    log.error("poisFallback:", e);
    return new HereMapsPoisResponse(List.of());
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
  @Retry(name = "hereMapsIsolineRequest", fallbackMethod = "isolineFallback")
  public IsochroneResponse isochroneFrom(IsochroneRequest input) {
    String url = buildIsolineUrl(input);

    try {
      ResponseEntity<IsolineResponse> response = restTemplate.getForEntity(url, IsolineResponse.class);
      return buildIsochroneResponse(response.getBody(), input);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("url: {}, input: {}", url, input);
      throw new RuntimeException("Failed to fetch isoline from HereMaps");
    }
  }

  public IsochroneResponse isolineFallback(IsochroneRequest input, RuntimeException e) {
    log.error("isolineFallback:", e);
    return new IsochroneResponse(List.of());
  }

  private IsochroneResponse buildIsochroneResponse(IsolineResponse input, IsochroneRequest request) {
    List<Isochrone> isochrones = new ArrayList<>();

    for (Isoline isoline : input.getIsolines()) {
      isochrones.add(toIsocrhone(isoline, request));
    }
    return new IsochroneResponse(isochrones);
  }

  private Isochrone toIsocrhone(Isoline isoline, IsochroneRequest request) {
    List<Coordinate> coords = new ArrayList<>();

    List<LatLngZ> locations = PolylineEncoderDecoder.decode(isoline.getPolygons().get(0).getOuter());
    for (LatLngZ loc : locations) {
      coords.add(new Coordinate(loc.lat, loc.lng));
    }

    return Isochrone.builder()
        .mode(request.getMode())
        .modeValue(isoline.getRange().getValue() / SECONDS_IN_MINUTE)
        .transportType(request.getTransportType())
        .polygon(new Polygon(coords))
        .center(request.getCenter())
        .build();
  }

  private String buildIsolineUrl(IsochroneRequest input) {
    // https://isoline.router.hereapi.com/v8/isolines?transportMode=car&origin=52.51578,13.37749&range[type]=time&range[values]=300

    String origin = String.format("%f,%f", input.getCenter().getLatitude(), input.getCenter().getLongitude());

    List<Integer> valuesList = input.getModeValues();
    if (input.getMode() == IsochroneMode.TIME_MINUTES) {
      valuesList = input.getModeValues().stream()
          .map(item -> item * SECONDS_IN_MINUTE)
          .collect(Collectors.toList());
    }

    String values = StringUtils.collectionToCommaDelimitedString(valuesList);

    StringBuilder urlBuilder = new StringBuilder(isolineUrl);
    urlBuilder.append("?transportMode=").append(isochroneTypeFrom(input.getTransportType()));
    urlBuilder.append("&origin=").append(origin);

    if (input.getDepartureTime() != null) {
      urlBuilder.append("&departureTime=")
          .append(DateTimeUtils.format(input.getDepartureTime(), "yyyy-MM-dd HH:mm:ss").replace(" ", "T"));
    }

    urlBuilder.append("&range[type]=").append(isochroneModeFrom(input.getMode()));
    urlBuilder.append("&range[values]=").append(values);

    if (input.getMaxPoints() != null) {
      urlBuilder.append("&shape[maxPoints]=").append(input.getMaxPoints());
    }

    if (input.getVehicle() != null && input.getVehicle().getSpeedKmPerHour() != null) {
      int segundosPorHora = 3600;
      int metrosEnKm = 1000;
      int metrosPorSegundo = Double.valueOf(input.getVehicle().getSpeedKmPerHour() * metrosEnKm / segundosPorHora)
          .intValue();
      urlBuilder.append("&vehicle[speedCap]=").append(metrosPorSegundo);
    }

    urlBuilder.append("&optimizeFor=performance");
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
      case TIME_MINUTES:
        return "time";
      case DISTANCE_METERS:
        return "distance";
      default:
        throw new NoSuchElementException("Element not exists");
    }
  }

}