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
import com.zonainmueble.reports.utils.NumberUtils;

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
    String wktIso5 = geometryUtils.polygonToWKT(findIsochrone(IsochroneTime.FIVE_MINUTES, walkIsochrones).getPolygon());
    String wktIso10 = geometryUtils.polygonToWKT(findIsochrone(IsochroneTime.TEN_MINUTES, walkIsochrones).getPolygon());
    String wktIso15 = geometryUtils
        .polygonToWKT(findIsochrone(IsochroneTime.FIFTEEN_MINUTES, walkIsochrones).getPolygon());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportParams(input, municipio, wktIso5));
    params.putAll(nseReportParams(wktIso5, wktIso10, wktIso15));

    // params.putAll(reportMapParams(input, walkIsochrones));

    return params;
  }

  private Map<String, Object> nseReportParams(String wktIso5, String wktIso10, String wktIso15) {
    List<NsePoblacion> nse = repository.nsePoblacion(wktIso5, wktIso10, wktIso15);
    NsePoblacion nseTop1 = nse.get(0);
    NsePoblacion nseTop2 = nse.get(1);
    NsePoblacion nseTop3 = nse.get(2);

    double iso5Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso5).sum();
    double iso10Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso10).sum();
    double iso15Poblacion = nse.stream().mapToDouble(NsePoblacion::getPobtot20Iso15).sum();

    double poblacionTotal = iso5Poblacion + iso10Poblacion + iso15Poblacion;
    double top1Poblacion = nseTop1.getPobtot20Iso5() + nseTop1.getPobtot20Iso10() + nseTop1.getPobtot20Iso15();
    double top2Poblacion = nseTop2.getPobtot20Iso5() + nseTop2.getPobtot20Iso10() + nseTop2.getPobtot20Iso15();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("nse_top1_poblacion_porcentaje", percentaje(top1Poblacion, poblacionTotal));
    params.put("nse_top2_poblacion_porcentaje", percentaje(top2Poblacion, poblacionTotal));
    params.put("nse_top1y2_poblacion_porcentaje", percentaje(top1Poblacion + top2Poblacion, poblacionTotal));

    params.put("nse_ingreso_promedio",
        NumberUtils.formatToInt((nseTop1.getImppheIso5() + nseTop1.getImppheIso10() + nseTop1.getImppheIso15()) / 3));

    params.put("nse_iso5_poblacion", NumberUtils.formatToInt(iso5Poblacion));
    params.put("nse_iso10_poblacion", NumberUtils.formatToInt(iso10Poblacion));
    params.put("nse_iso15_poblacion", NumberUtils.formatToInt(iso15Poblacion));
    params.put("nse_top_1_desc", nseTop1.getDescripcion());
    params.put("nse_top_1_nombre", nseTop1.getNombre());
    params.put("nse_top_1_nse", nseTop1.getNse());
    params.put("nse_top_1_color", nseTop1.getColorHex());
    params.put("nse_top_2_desc", nseTop2.getDescripcion());
    params.put("nse_top_2_nombre", nseTop2.getNombre());
    params.put("nse_top_2_nse", nseTop2.getNse());
    params.put("nse_top_2_color", nseTop2.getColorHex());
    params.put("nse_top_3_desc", nseTop3.getDescripcion());
    params.put("nse_top_3_nombre", nseTop3.getNombre());
    params.put("nse_top_3_nse", nseTop3.getNse());
    params.put("nse_top_3_color", nseTop3.getColorHex());

    params.put("nse_iso5_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso5()));
    params.put("nse_iso5_top_1_porcentaje", percentaje(nseTop1.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso10()));
    params.put("nse_iso10_top_1_porcentaje", percentaje(nseTop1.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_1_poblacion", NumberUtils.formatToInt(nseTop1.getPobtot20Iso15()));
    params.put("nse_iso15_top_1_porcentaje", percentaje(nseTop1.getPobtot20Iso15(), iso15Poblacion));

    params.put("nse_iso5_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso5()));
    params.put("nse_iso5_top_2_porcentaje", percentaje(nseTop2.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso10()));
    params.put("nse_iso10_top_2_porcentaje", percentaje(nseTop2.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_2_poblacion", NumberUtils.formatToInt(nseTop2.getPobtot20Iso15()));
    params.put("nse_iso15_top_2_porcentaje", percentaje(nseTop2.getPobtot20Iso15(), iso15Poblacion));

    params.put("nse_iso5_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso5()));
    params.put("nse_iso5_top_3_porcentaje", percentaje(nseTop3.getPobtot20Iso5(), iso5Poblacion));
    params.put("nse_iso10_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso10()));
    params.put("nse_iso10_top_3_porcentaje", percentaje(nseTop3.getPobtot20Iso10(), iso10Poblacion));
    params.put("nse_iso15_top_3_poblacion", NumberUtils.formatToInt(nseTop3.getPobtot20Iso15()));
    params.put("nse_iso15_top_3_porcentaje", percentaje(nseTop3.getPobtot20Iso15(), iso15Poblacion));

    return params;
  }

  private Map<String, Object> reportMapParams(ReportRequest input, List<Isochrone> walkIsochrones) {
    List<Marker> markers = List.of(new Marker(new Coordinate(input.getLatitude(), input.getLongitude())));

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] image1 = mapImageService.image(new MapImageRequest(447, 263, "roadmap", markers, null));
    params.put("mapImage1", image1);

    Isochrone fiveMinIso = findIsochrone(IsochroneTime.FIVE_MINUTES, walkIsochrones);
    byte[] image2 = mapImageService
        .image(new MapImageRequest(232, 280, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage2", image2);

    byte[] image3 = mapImageService
        .image(new MapImageRequest(232, 434, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage3", image3);

    // Isochrone tenMinIso = findIsochrone(IsochroneTime.TEN_MINUTES,
    // walkIsochrones);
    // Isochrone fifMinIso = findIsochrone(IsochroneTime.FIFTEEN_MINUTES,
    // walkIsochrones);
    // byte[] image4 = mapImageService.image(new MapImageRequest(255, 270,
    // "roadmap", markers, List.of(fiveMinIso.getPolygon(), tenMinIso.getPolygon(),
    // fifMinIso.getPolygon())));
    // params.put("mapImage4", image4);

    // byte[] image5 = mapImageService.image(new MapImageRequest(365, 300, "hybrid",
    // markers, polygons));
    // params.put("mapImage5", image5);

    return params;
  }

  private Map<String, Object> basicReportParams(ReportRequest input, Municipio municipio, String wktIso5) {
    Poblacion poblacion = repository.poblacion(wktIso5);
    List<PoblacionPorcentajeEstudios> pEstudios = repository.poblacionPorcentajeEstudios(wktIso5,
        municipio.getClaveEdo());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportService.generalParams(input, municipio));
    params.putAll(basicReportService.poblacionParams(poblacion));
    params.putAll(basicReportService.grupoEdadParams(wktIso5));
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

  private String percentaje(double value, double total) {
    double percent = 0;
    if (total != 0) {
      percent = (value / total) * 100;
    }
    return NumberUtils.formatToDecimal(percent);
  }

}
