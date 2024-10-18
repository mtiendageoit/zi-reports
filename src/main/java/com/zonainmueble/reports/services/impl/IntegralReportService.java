package com.zonainmueble.reports.services.impl;

import static com.zonainmueble.reports.enums.IsochroneTime.*;

import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.config.AppConfig;
import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.dto.Marker.MarkerSize;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.maps.here.HereMapsService;
import com.zonainmueble.reports.maps.here.pois.HereMapsPoisResponse;
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
  private final HereMapsService hereMapsService;
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

    Isochrone iso5 = findIsochrone(IsochroneTime.FIVE_MINUTES, walkIsochrones);

    String wktIso5 = geometryUtils.polygonToWKT(iso5.getPolygon());
    String wktIso10 = geometryUtils.polygonToWKT(findIsochrone(IsochroneTime.TEN_MINUTES, walkIsochrones).getPolygon());
    String wktIso15 = geometryUtils
        .polygonToWKT(findIsochrone(IsochroneTime.FIFTEEN_MINUTES, walkIsochrones).getPolygon());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(basicReportParams(input, municipio, wktIso5, iso5));
    params.putAll(nseReportParams(wktIso5, wktIso10, wktIso15));

    params.putAll(reportBasicMapParams(input, walkIsochrones));

    params.putAll(poisMovCaminandoParams(input, walkIsochrones));

    params.putAll(poisMovAutomovilParams(input));

    return params;
  }

  private Map<String, Object> poisMovAutomovilParams(ReportRequest input) {
    Map<String, Object> params = new HashMap<String, Object>();

    Coordinate center = new Coordinate(input.getLatitude(), input.getLongitude());

    LocalDateTime domingo7am = common.getPreviousFromNowPlus(DayOfWeek.SUNDAY, 7);
    List<Isochrone> isos7am = isochronesAutomovil(center, domingo7am);
    LocalDateTime lunes2pm = common.getPreviousFromNowPlus(DayOfWeek.MONDAY, 14);
    List<Isochrone> isos2pm = isochronesAutomovil(center, lunes2pm);

    List<Marker> markers = List.of(new Marker(center));

    List<Polygon> polys = isos7am.stream().map(i -> i.getPolygon()).collect(Collectors.toList());
    byte[] image1 = mapImageService.image(new MapImageRequest(326, 350, "roadmap", markers, polys));
    params.put("mapImageMovAutoDom7AM", image1);

    polys = isos2pm.stream().map(i -> i.getPolygon()).collect(Collectors.toList());
    byte[] image2 = mapImageService.image(new MapImageRequest(326, 350, "roadmap", markers, polys));
    params.put("mapImageMovAutoLun2PM", image2);

    List<PoisCategory> categories = repository.poisMovilidadAutomovil();
    PoisRequest request = new PoisRequest();
    request.setCenter(center);
    request.setBoundingBox(geometryUtils.boundingBox(findIsochrone(TEN_MINUTES, isos7am).getPolygon()));
    request.setCategories(categories.stream().map(i -> i.getKey()).collect(Collectors.toList()));
    request.setLimit(100);
    HereMapsPoisResponse response = hereMapsService.pois(request);
    List<Marker> gasolineras = response.getItems().stream().map(
        i -> new Marker(new Coordinate(i.getPosition().getLat(),
            i.getPosition().getLng()), "#FBB700", MarkerSize.small))
        .collect(Collectors.toList());

    gasolineras.addAll(markers);

    byte[] image3 = mapImageService.image(new MapImageRequest(448, 448, "roadmap", gasolineras, null));
    params.put("mapImageMovGasolineras", image3);

    return params;
  }

  private List<Isochrone> isochronesAutomovil(Coordinate center, LocalDateTime departureTime) {
    List<Isochrone> isos = isochronesFrom(center,
        List.of(TEN_MINUTES.getValue(), THIRTY_MINUTES.getValue(), SIXTY_MINUTES.getValue()),
        TransportType.DRIVING, departureTime);

    Isochrone iso1 = findIsochrone(TEN_MINUTES, isos);
    iso1.getPolygon().setStyle(new PolygonStyle("#A3DA91FF", "#A3DA9144"));

    Isochrone iso2 = findIsochrone(THIRTY_MINUTES, isos);
    iso2.getPolygon().setStyle(new PolygonStyle("#E3E367FF", "#E3E36744"));

    Isochrone iso3 = findIsochrone(SIXTY_MINUTES, isos);
    iso3.getPolygon().setStyle(new PolygonStyle("#9BC1DBFF", "#9BC1DB44"));

    return isos;
  }

  private List<Isochrone> isochronesFrom(Coordinate center, List<Integer> modeValues,
      TransportType transportType, LocalDateTime departureTime) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(center)
        .modeValues(modeValues)
        .departureTime(departureTime)
        .transportType(transportType)
        .maxPoints(350)
        .mode(IsochroneMode.TIME_MINUTES)
        .build();

    return isochroneService.isochroneFrom(request).getIsochrones();
  }

  private Map<String, Object> poisMovCaminandoParams(ReportRequest input, List<Isochrone> walkIsochrones) {
    Map<String, Object> params = new HashMap<String, Object>();
    List<PoisCategory> categories = repository.poisMovilidadCaminando();

    PoisRequest request = new PoisRequest();
    request.setCenter(new Coordinate(input.getLatitude(), input.getLongitude()));
    request.setBoundingBox(geometryUtils.boundingBox(findIsochrone(FIFTEEN_MINUTES, walkIsochrones).getPolygon()));
    request.setCategories(categories.stream().map(i -> i.getKey()).collect(Collectors.toList()));
    request.setLimit(100);

    HereMapsPoisResponse response = hereMapsService.pois(request);

    List<Marker> markers = response.getItems().stream().map(
        i -> new Marker(new Coordinate(i.getPosition().getLat(),
            i.getPosition().getLng()), "#FBB700", MarkerSize.small))
        .collect(Collectors.toList());

    markers.add(new Marker(request.getCenter()));

    byte[] image = mapImageService.image(new MapImageRequest(448, 448, "roadmap", markers, null));
    params.put("mapImageMovCaminando", image);

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

  private Map<String, Object> reportBasicMapParams(ReportRequest input, List<Isochrone> walkIsochrones) {
    List<Marker> markers = List.of(new Marker(new Coordinate(input.getLatitude(), input.getLongitude())));

    Map<String, Object> params = new HashMap<String, Object>();
    byte[] image1 = mapImageService.image(new MapImageRequest(447, 263, "roadmap", markers, null));
    params.put("mapImage1", image1);

    Isochrone fiveMinIso = findIsochrone(FIVE_MINUTES, walkIsochrones);
    byte[] image2 = mapImageService
        .image(new MapImageRequest(232, 280, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage2", image2);

    byte[] image3 = mapImageService
        .image(new MapImageRequest(232, 434, "roadmap", markers, List.of(fiveMinIso.getPolygon())));
    params.put("mapImage3", image3);

    fiveMinIso = setStyle(fiveMinIso, FIVE_MINUTES);
    Isochrone tenMinIso = setStyle(findIsochrone(TEN_MINUTES, walkIsochrones), TEN_MINUTES);
    Isochrone fifMinIso = setStyle(findIsochrone(FIFTEEN_MINUTES, walkIsochrones), FIFTEEN_MINUTES);

    byte[] image4 = mapImageService.image(
        new MapImageRequest(232, 416, "roadmap", markers, List.of(fifMinIso.getPolygon(), tenMinIso.getPolygon(),
            fiveMinIso.getPolygon())));
    params.put("mapImage4", image4);

    return params;
  }

  private Isochrone setStyle(Isochrone isochrone, IsochroneTime time) {
    if (time == IsochroneTime.FIVE_MINUTES) {
      isochrone.getPolygon().setStyle(new PolygonStyle("#A3DA91FF", "#A3DA9144"));
    } else if (time == IsochroneTime.TEN_MINUTES) {
      isochrone.getPolygon().setStyle(new PolygonStyle("#E3E367FF", "#E3E36744"));
    } else if (time == IsochroneTime.FIFTEEN_MINUTES) {
      isochrone.getPolygon().setStyle(new PolygonStyle("#9BC1DBFF", "#9BC1DB44"));
    } else {
      throw new NoSuchElementException("Element not found");
    }
    return isochrone;
  }

  private Map<String, Object> basicReportParams(ReportRequest input, Municipio municipio, String wktIso5,
      Isochrone iso5) {
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
    params.putAll(basicReportService.reportPoisParams(input, iso5.getPolygon()));
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
        .modeValues(List.of(FIVE_MINUTES.getValue(), TEN_MINUTES.getValue(), FIFTEEN_MINUTES.getValue()))
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
