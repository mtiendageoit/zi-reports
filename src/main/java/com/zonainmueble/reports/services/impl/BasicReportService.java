package com.zonainmueble.reports.services.impl;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.*;
import com.zonainmueble.reports.exceptions.*;
import com.zonainmueble.reports.models.*;
import com.zonainmueble.reports.repositories.*;
import com.zonainmueble.reports.services.*;
import com.zonainmueble.reports.utils.*;

import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
@AllArgsConstructor
public class BasicReportService implements ReportService {
  private final int FIVE_MINUTES = 5;
  private final String JASPER_REPORT_PATH = "/static/reports/basic/basic.jasper";

  private final BasicReportRepository repository;

  private final PoisService poisService;
  private final MapImageService mapImageService;
  private final IsochroneService isochroneService;
  private final JasperReportService jasperReportService;

  @Override
  public byte[] generateReport(ReportRequest input) {
    Estado estado = repository.estado(input.getLatitude(), input.getLongitude())
        .orElseThrow(() -> new ReportException("COORDINATES_OUT_OF_ALLOWED_REGION",
            "The coordinates are out of the allowed region."));

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input, estado));
    return jasperReportService.generatePdf(JASPER_REPORT_PATH, params);
  }

  private Map<String, Object> reportParams(ReportRequest input, Estado estado) {
    Polygon iso = isochrone(input);
    String wkt = ReportsUtils.polygonToWKT(iso);
    Poblacion poblacion = repository.poblacion(wkt);
    List<PoblacionPorcentajeEstudios> pEstudios = repository.poblacionPorcentajeEstudios(wkt, estado.getClaveEdo());

    Map<String, Object> params = new HashMap<String, Object>();
    params.putAll(generalParams(input, estado));
    params.putAll(poblacionParams(poblacion));
    params.putAll(grupoEdadParams(wkt));
    params.putAll(poblacionResumenParams(estado, poblacion));
    params.putAll(poblacionPorcentajeEstudiosParams(pEstudios));
    params.putAll(graficaPorcentajeEstudiosParams(pEstudios));
    params.putAll(reportPoisParams());
    params.putAll(reportMapParams(input, iso));

    return params;
  }

  private Map<String, Object> generalParams(ReportRequest input, Estado estado) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("address", input.getAddress());
    params.put("nombre_edo", estado.getNombre());
    params.put("build_time", ReportsUtils.reportBuildTime());
    return params;
  }

  private Map<String, Object> poblacionParams(Poblacion pob) {

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("habitantes", NumberUtils.formatToInt(pob.getPobtot20()));
    params.put("habitantes2010", NumberUtils.formatToInt(pob.getPobtot10()));
    params.put("ninos", NumberUtils.formatToInt(pob.getNinos()));
    params.put("hombres", NumberUtils.formatToInt(pob.getHombres()));
    params.put("pHombres", NumberUtils.formatToInt(pob.getPHombres()));
    params.put("mujeres", NumberUtils.formatToInt(pob.getMujeres()));
    params.put("pMujeres", NumberUtils.formatToInt(pob.getPMujeres()));

    params.put("familias", NumberUtils.formatToInt(pob.getTothog20()));
    params.put("viviendas", NumberUtils.formatToInt(pob.getVivtot20()));
    params.put("habitantes_vivienda", NumberUtils.formatToDecimal(pob.getHabitantesVivienda()));

    params.put("viviendas_habitadas", NumberUtils.formatToInt(pob.getTvivpah20()));
    params.put("viviendas_1_dormitorio", NumberUtils.formatToInt(pob.getVph1dor20()));
    params.put("viviendas_2mas_dormitorios", NumberUtils.formatToInt(pob.getVph2ymd20()));

    params.put("gentrificacion", NumberUtils.formatToInt(pob.getGentrificacion()));

    return params;
  }

  private Map<String, Object> grupoEdadParams(String isoWKT) {
    GrupoEdad data = repository.grupoEdad(isoWKT);

    double total = data.getP0a2f20() + data.getP3a520() + data.getP6a1120() +
        data.getP12a1420() + data.getP15a1720() + data.getP18a2420() + data.getPob156420() +
        data.getPmay6020() + data.getPob65ma20();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("habitantes_0a2", percentajeInt(data.getP0a2f20(), total));
    params.put("habitantes_3a5", percentajeInt(data.getP3a520(), total));
    params.put("habitantes_6a11", percentajeInt(data.getP6a1120(), total));
    params.put("habitantes_12a14", percentajeInt(data.getP12a1420(), total));
    params.put("habitantes_15a17", percentajeInt(data.getP15a1720(), total));
    params.put("habitantes_18a24", percentajeInt(data.getP18a2420(), total));
    params.put("habitantes_15a64", percentajeInt(data.getPob156420(), total));
    params.put("habitantes_mas60", percentajeInt(data.getPmay6020(), total));
    params.put("habitantes_mas65", percentajeInt(data.getPob65ma20(), total));

    return params;
  }

  private Map<String, Object> poblacionResumenParams(Estado edo, Poblacion pob) {
    PoblacionResumen data = repository.poblacionResumen(edo.getClaveEdo(), pob);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("zona_habitantes", data.getHabitantesDesc());
    params.put("zona_habitantes_infantil", data.getHabitantesInfantilDesc());
    params.put("zona_densidad", data.getDensidadDesc());
    return params;
  }

  private Map<String, Object> poblacionPorcentajeEstudiosParams(List<PoblacionPorcentajeEstudios> pEstudios) {

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("zona_peh_porcentaje_1", NumberUtils.formatToInt(pEstudios.get(0).getPParticipacionRango() * 100));
    params.put("zona_peh_desc_1", pEstudios.get(0).getDescripcion());
    params.put("zona_peh_porcentaje_edo_1", NumberUtils.formatToInt(pEstudios.get(0).getPParticipacionEdo() * 100));

    params.put("zona_peh_porcentaje_2", NumberUtils.formatToInt(pEstudios.get(1).getPParticipacionRango() * 100));
    params.put("zona_peh_desc_2", pEstudios.get(1).getDescripcion());
    params.put("zona_peh_porcentaje_edo_2", NumberUtils.formatToInt(pEstudios.get(1).getPParticipacionEdo() * 100));

    params.put("zona_peh_grado_promedio_1", NumberUtils.formatToInt(pEstudios.get(0).getGrpesc20Isocrona()));
    return params;
  }

  private Map<String, Object> graficaPorcentajeEstudiosParams(List<PoblacionPorcentajeEstudios> pEstudios) {

    List<CategoryData> data = new ArrayList<>();

    for (PoblacionPorcentajeEstudios item : pEstudios) {
      data.add(new CategoryData(item.getDescripcionCorta(), item.getPParticipacionRango() * 100));
    }

    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("grafica_dataset", dataSource);
    return params;
  }

  private Map<String, Object> reportPoisParams() {
    PoisResponse response = poisService.pois();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("pois_atm", response.getPois().get(0).getTotal());
    params.put("pois_convenience_store", response.getPois().get(1).getTotal());

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

  private Polygon isochrone(ReportRequest input) {
    IsochroneRequest request = IsochroneRequest.builder()
        .center(new Coordinate(input.getLatitude(), input.getLongitude()))
        .transportType(TransportType.WALKING)
        .mode(IsochroneMode.TIME)
        .modeValues(List.of(FIVE_MINUTES))
        .build();

    return isochroneService.isochroneFrom(request).getPolygons().get(0);
  }

  private String percentajeInt(double value, double total) {
    double percent = 0;
    if (total != 0) {
      percent = (value / total) * 100;
    }
    return NumberUtils.formatToInt(value) + " (" + NumberUtils.formatToInt(percent) + "%)";
  }

}
