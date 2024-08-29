package com.zonainmueble.reports.services;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.ReportRequestDto;
import com.zonainmueble.reports.maps.FeatureCollection;
import com.zonainmueble.reports.maps.GoogleMapsProvider;
import com.zonainmueble.reports.maps.ImageRequest;
import com.zonainmueble.reports.maps.MapboxMapProvider;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BasicReportService implements IReportService {
  private final String JASPER_REPORT_PATH = "/static/reports/basic/basic.jasper";

  private final JasperReport jasperReport;
  private final GoogleMapsProvider googleMaps;
  private final MapboxMapProvider mapbox;

  @Override
  public byte[] generateReport(ReportRequestDto input) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input));
    return jasperReport.generatePdf(JASPER_REPORT_PATH, params);
  }

  private Map<String, Object> reportParams(ReportRequestDto input) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("address", input.getAddress());

    FeatureCollection coll = mapbox.isocrone(input.getLatitude(), input.getLongitude(), 5);

    byte[] imagen1 = googleMaps.image(imageRequest(input, 310, 165, true), coll);
    params.put("mapImage1", imagen1);

    byte[] imagen2 = googleMaps.image(imageRequest(input, 350, 370, true), coll);
    params.put("mapImage2", imagen2);

    byte[] imagen3 = googleMaps.image(imageRequest(input, 350, 300, false), coll);
    params.put("mapImage3", imagen3);

    byte[] imagen4 = googleMaps.image(imageRequest(input, 255, 270, true), coll);
    params.put("mapImage4", imagen4);

    byte[] imagen5 = googleMaps.image(imageRequest(input, 365, 300, true), coll);
    params.put("mapImage5", imagen5);

    return params;
  }

  private ImageRequest imageRequest(ReportRequestDto input, int width, int height, boolean isoVisible) {
    ImageRequest item = new ImageRequest();
    item.setLatitude(input.getLatitude());
    item.setLongitude(input.getLongitude());
    item.setWidth(width);
    item.setHeight(height);
    item.setIsoVisible(isoVisible);

    return item;
  }
}
