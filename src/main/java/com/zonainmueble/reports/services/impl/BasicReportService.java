package com.zonainmueble.reports.services.impl;

import java.util.*;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.services.*;
import com.zonainmueble.reports.statistics.*;
import com.zonainmueble.reports.utils.NumberUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BasicReportService implements ReportService {
  private final int FIVE_MINUTES = 5;
  private final String JASPER_REPORT_PATH = "/static/reports/basic/basic.jasper";

  private final PopulationRepository populationRepository;
  private final PopulationRangeRepository populationRangeRepository;

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
    params.putAll(reportDataParams(input, isochrone));

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

  private Map<String, Object> reportDataParams(ReportRequest input, Polygon isochrone) {
    String isoWKT = isochroneToWKT(isochrone);

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(populationReportParams(isoWKT));
    params.putAll(studyReportParams(isoWKT));
    params.putAll(populationRangesReportParams(isoWKT));

    return params;
  }

  private Map<String, Object> populationRangesReportParams(String isoWKT) {
    PopulationRange range = populationRangeRepository.findPopulationRanges(isoWKT).get(0);

    double total = range.getP0a2f20() + range.getP3a520() + range.getP6a1120() + range.getP12a1420()
        + range.getP15a1720() + range.getP18a2420() + range.getPob156420() + range.getPmay6020() + range.getPob65ma20();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("habitantes_0a2", formatIntPercentaje(range.getP0a2f20(), total));
    params.put("habitantes_3a5", formatIntPercentaje(range.getP3a520(), total));
    params.put("habitantes_6a11", formatIntPercentaje(range.getP6a1120(), total));
    params.put("habitantes_12a14", formatIntPercentaje(range.getP12a1420(), total));
    params.put("habitantes_15a17", formatIntPercentaje(range.getP15a1720(), total));
    params.put("habitantes_18a24", formatIntPercentaje(range.getP18a2420(), total));
    params.put("habitantes_15a64", formatIntPercentaje(range.getPob156420(), total));
    params.put("habitantes_mas60", formatIntPercentaje(range.getPmay6020(), total));
    params.put("habitantes_mas65", formatIntPercentaje(range.getPob65ma20(), total));

    return params;
  }

  private String formatIntPercentaje(double value, double total) {
    double percent = 0;
    if (total != 0) {
      percent = (value / total) * 100;
    }
    return NumberUtils.formatToInt(value) + " (" + NumberUtils.formatToInt(percent) + "%)";
  }

  private Map<String, Object> studyReportParams(String isoWKT) {
    Double years = populationRepository.findYearsOfStudy(isoWKT);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("anios_estudio", NumberUtils.formatToInt(years));

    return params;
  }

  private Map<String, Object> populationReportParams(String isoWKT) {
    Population population = populationRepository.findPopulationFrom(isoWKT).get(0);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("habitantes", NumberUtils.formatToInt(population.getPobtot20()));
    params.put("habitantes2010", NumberUtils.formatToInt(population.getPobtot10()));
    params.put("ninos", NumberUtils.formatToInt(population.getNinos()));
    params.put("hombres", NumberUtils.formatToInt(population.getHombres()));
    params.put("pHombres", NumberUtils.formatToInt(population.getPHombres()));
    params.put("mujeres", NumberUtils.formatToInt(population.getMujeres()));
    params.put("pMujeres", NumberUtils.formatToInt(population.getPMujeres()));

    params.put("familias", NumberUtils.formatToInt(population.getTothog20()));
    params.put("viviendas", NumberUtils.formatToInt(population.getVivtot20()));
    params.put("habitantes_vivienda", NumberUtils.formatToDecimal(population.getHabitantesVivienda()));

    params.put("viviendas_habitadas", NumberUtils.formatToInt(population.getTvivpah20()));
    params.put("viviendas_1_dormitorio", NumberUtils.formatToInt(population.getVph1dor20()));
    params.put("viviendas_2mas_dormitorios", NumberUtils.formatToInt(population.getVph2ymd20()));

    params.put("gentrificacion", NumberUtils.formatToInt(population.getGentrificacion()));

    return params;
  }

  private String isochroneToWKT(Polygon isochrone) {
    String wkt = Strings.EMPTY;
    return wkt;
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
