package com.zonainmueble.reports.services.impl;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.config.AppConfig;
import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.models.*;
import com.zonainmueble.reports.repositories.*;
import com.zonainmueble.reports.services.*;
import com.zonainmueble.reports.utils.GeometryUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class IntegralReportService implements ReportService {
  private final String JASPER_REPORT_PATH = "/static/reportes/integral/integral.jasper";

  private final ReportRepository repository;

  private final AppConfig config;
  private final CommonReportService common;
  private final JasperReportService jasper;
  private final GeometryUtils geometryUtils;
  private final IsochroneService isochroneService;
  private final BasicReportService basicReportService;

  @Override
  public byte[] generateReport(ReportRequest input) {
    Municipio municipio = common.municipioFrom(input.getLatitude(), input.getLongitude());
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input, municipio));
    return jasper.generatePdf(JASPER_REPORT_PATH, params);
  }

  private Map<String, Object> reportParams(ReportRequest input, Municipio municipio) {
    List<Isochrone> walkIsochrones = walkIsochronesFrom(input);

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportParams(input, municipio, walkIsochrones));

    return params;
  }

  private Map<String, Object> basicReportParams(ReportRequest input, Municipio municipio,
      List<Isochrone> walkIsochrones) {
    Isochrone iso5Minutes = walkIsochrones.stream().filter(item -> item.getModeValue() == 5).findFirst()
        .orElseThrow(() -> new NoSuchElementException("Element not found"));

    Polygon iso = geometryUtils.buffer(iso5Minutes.getPolygon(), config.getIsochroneBufferMeters());
    String wkt = geometryUtils.polygonToWKT(iso);

    Poblacion poblacion = repository.poblacion(wkt);
    List<PoblacionPorcentajeEstudios> pEstudios = repository.poblacionPorcentajeEstudios(wkt, municipio.getClaveEdo());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportService.generalParams(input, municipio));
    params.putAll(basicReportService.poblacionParams(poblacion));
    params.putAll(basicReportService.grupoEdadParams(wkt));
    params.putAll(basicReportService.poblacionResumenParams(municipio, poblacion));
    params.putAll(basicReportService.poblacionPorcentajeEstudiosParams(pEstudios));
    params.putAll(basicReportService.precioMetroCuadradoParams(municipio));
    // params.putAll(basicReportService.conclusionParams(params));

    return params;
  }

  private List<Isochrone> walkIsochronesFrom(ReportRequest input) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .mode(IsochroneMode.TIME_MINUTES)
        .modeValues(List.of(5, 10, 15))
        .transportType(TransportType.WALKING)
        .build();

    return isochroneService.isochroneFrom(request).getIsochrones();
  }

}
