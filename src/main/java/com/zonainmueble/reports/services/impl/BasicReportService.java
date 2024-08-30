package com.zonainmueble.reports.services.impl;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.services.*;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BasicReportService implements ReportService {
  private final int FIVE_MINUTES = 5;
  private final String JASPER_REPORT_PATH = "/static/reports/basic/basic.jasper";

  private final MapImageService mapImageService;
  private final IsochroneService isochroneService;
  private final JasperReportService jasperReportService;

  @Override
  public byte[] generateReport(ReportRequest input) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input));
    return jasperReportService.generatePdf(JASPER_REPORT_PATH, params);
  }

  private Map<String, Object> reportParams(ReportRequest input) {
    Polygon isochrone = isochroneFrom(input);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("address", input.getAddress());

    params.putAll(reportMapParams(input, isochrone));

    return params;
  }

  private Map<String, Object> reportMapParams(ReportRequest input, Polygon isochrone) {
    List<Polygon> polygons = List.of(isochrone);
    List<Marker> markers = List.of(new Marker(new Coordinate(input.getLatitude(), input.getLongitude())));

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] image1 = mapImageService.image(new MapImageRequest(310, 165, markers, polygons));
    params.put("mapImage1", image1);

    byte[] image2 = mapImageService.image(new MapImageRequest(350, 370, markers, polygons));
    params.put("mapImage2", image2);

    byte[] image3 = mapImageService.image(new MapImageRequest(350, 300, markers, null));
    params.put("mapImage3", image3);

    byte[] image4 = mapImageService.image(new MapImageRequest(255, 270, markers, polygons));
    params.put("mapImage4", image4);

    byte[] image5 = mapImageService.image(new MapImageRequest(365, 300, markers, polygons));
    params.put("mapImage5", image5);

    return params;
  }

  private Polygon isochroneFrom(ReportRequest input) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .transportType(TransportType.WALKING)
        .mode(IsochroneMode.TIME)
        .modeValues(List.of(FIVE_MINUTES))
        .build();

    return isochroneService.isochroneFrom(request).getPolygons().get(0);
  }

}
