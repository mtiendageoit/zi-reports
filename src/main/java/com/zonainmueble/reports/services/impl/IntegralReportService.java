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
  private final MapImageService mapImageService;
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
    List<Isochrone> walkIsochrones = bufferToIsochrones(walkIsochronesFrom(input));

    Map<String, Object> params = new HashMap<String, Object>();
    // params.putAll(basicReportParams(input, municipio, walkIsochrones));

    params.putAll(reportMapParams(input, walkIsochrones));

    return params;
  }

  private Map<String, Object> reportMapParams(ReportRequest input, List<Isochrone> walkIsochrones) {
    List<Marker> markers = List.of(new Marker(new Coordinate(input.getLatitude(), input.getLongitude())));

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] image1 = mapImageService.image(new MapImageRequest(320, 276, "roadmap", markers, null));
    params.put("mapImage1", image1);

    Isochrone fiveMinIso = findIsochrone(IsochroneTime.FIVE_MINUTES, walkIsochrones);
    byte[] image2 = mapImageService
        .image(new MapImageRequest(350, 370, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage2", image2);

    byte[] image3 = mapImageService
        .image(new MapImageRequest(255, 270, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage3", image3);

    Isochrone tenMinIso = findIsochrone(IsochroneTime.TEN_MINUTES, walkIsochrones);
    Isochrone fifMinIso = findIsochrone(IsochroneTime.FIFTEEN_MINUTES, walkIsochrones);
    byte[] image4 = mapImageService.image(new MapImageRequest(255, 270,
        "roadmap", markers, List.of(fiveMinIso.getPolygon(), tenMinIso.getPolygon(), fifMinIso.getPolygon())));
    params.put("mapImage4", image4);

    // byte[] image5 = mapImageService.image(new MapImageRequest(365, 300, "hybrid",
    // markers, polygons));
    // params.put("mapImage5", image5);

    return params;
  }

  private Map<String, Object> basicReportParams(ReportRequest input, Municipio municipio,
      List<Isochrone> walkIsochrones) {
    Isochrone fiveMinIso = findIsochrone(IsochroneTime.FIVE_MINUTES, walkIsochrones);
    String wkt = geometryUtils.polygonToWKT(fiveMinIso.getPolygon());

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

  private Isochrone findIsochrone(IsochroneTime timeValue, List<Isochrone> isochrones) {
    return isochrones.stream().filter(item -> item.getModeValue() == timeValue.getValue()).findFirst()
        .orElseThrow(() -> new NoSuchElementException("Element not found"));
  }

  private List<Isochrone> bufferToIsochrones(List<Isochrone> isochrones) {
    for (Isochrone isochrone : isochrones) {
      isochrone.setPolygon(geometryUtils.buffer(isochrone.getPolygon(),
          config.getIsochroneBufferMeters()));
    }
    return isochrones;
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
