package com.zonainmueble.reports.services;

import java.util.*;
import java.time.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.enums.TransportType;
import com.zonainmueble.reports.exceptions.BaseException;
import com.zonainmueble.reports.models.Municipio;
import com.zonainmueble.reports.repositories.ReportRepository;
import com.zonainmueble.reports.utils.DateTimeUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommonReportService {
  private ReportRepository repository;

  public Municipio municipioFrom(double latitude, double longitude) {
    return repository.municipio(latitude, longitude)
        .orElseThrow(() -> new NoSuchElementException("Municipio not found: " + latitude + "," + longitude));
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
