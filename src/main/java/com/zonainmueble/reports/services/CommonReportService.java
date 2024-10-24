package com.zonainmueble.reports.services;

import java.util.*;
import java.time.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.Coordinate;
import com.zonainmueble.reports.dto.Marker;
import com.zonainmueble.reports.dto.Marker.MarkerSize;
import com.zonainmueble.reports.enums.TransportType;
import com.zonainmueble.reports.exceptions.*;
import com.zonainmueble.reports.maps.here.pois.HereMapsPoisResponse;
import com.zonainmueble.reports.maps.here.pois.Poi;
import com.zonainmueble.reports.models.Municipio;
import com.zonainmueble.reports.models.PoisCategory;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.utils.DateTimeUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommonReportService {
  private ReportRepository repository;

  public List<Marker> markers(HereMapsPoisResponse poisResponse, List<PoisCategory> categories) {
    List<Marker> markers = new ArrayList<>();

    if (poisResponse != null && poisResponse.getItems() != null && !poisResponse.getItems().isEmpty()) {
      for (Poi poi : poisResponse.getItems()) {

        for (PoisCategory cat : categories) {
          if (poi.anyCategoryIs(cat.getKey())) {
            markers.add(Marker.builder()
                .coordinate(new Coordinate(poi.getPosition().getLat(), poi.getPosition().getLng()))
                .color(cat.getColor())
                .size(MarkerSize.small)
                .distance(poi.getDistance())
                .category(cat.getKey())
                .build());
            break;
          }
        }
      }
    }
    return markers;
  }

  public Municipio municipioFrom(double latitude, double longitude) {
    return repository.municipio(latitude, longitude)
        .orElseThrow(() -> new LocationDataUnavailableException("LOCATION_DATA_UNAVAILABLE",
            "No se puede generar el reporte para la ubicación enviada. latitude: " + latitude + ", longitude: "
                + longitude));
  }

  public LocalDateTime getPreviousFromNowPlus(DayOfWeek dayOfWeek, long hours) {
    LocalDate today = DateTimeUtils.getPreviousFromNow(dayOfWeek).toLocalDate();
    return today.atStartOfDay().plusHours(hours);
  }

  public Map<String, String> reportBuildTime() {
    LocalDateTime now = DateTimeUtils.now();
    String date = DateTimeUtils.format(now, "dd MMMM'.' yyyy");
    date = date.substring(0, 3) + date.substring(3, 4).toUpperCase() + date.substring(4);
    String time = DateTimeUtils.format(now, "HH:mm 'hrs'");

    return Map.of("date", date, "time", time);
  }

  public String transportType(TransportType type) {
    switch (type) {
      case WALKING:
        return "caminando";
      case CYCLING:
        return "en bicicleta";
      case DRIVING:
      case DRIVING_TRAFFIC:
        return "en auto";
      default:
        throw new BaseException("INVALID_TRANSPORT_TYPE", "The transport type no exists");
    }
  }

}
