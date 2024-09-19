package com.zonainmueble.reports.repositories;

import java.util.*;

import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import com.zonainmueble.reports.models.*;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ReportRepository {
  private final JdbcTemplate jdbcTemplate;

  public Optional<Municipio> municipio(double latitude, double longitude) {
    String sql = "SELECT * FROM public.___zi_municipio_intersectado(?, ?)";

    List<Municipio> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Municipio.class), latitude,
        longitude);

    if (data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(data.get(0));
  }

  public Poblacion poblacion(String isochroneWKT) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_poblacion(?)";
    List<Poblacion> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Poblacion.class), isochroneWKT);
    return data.get(0);
  }

  public GrupoEdad grupoEdad(String isochroneWKT) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_grupos_edad(?)";
    List<GrupoEdad> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(GrupoEdad.class), isochroneWKT);
    return data.get(0);
  }

  public PoblacionResumen poblacionResumen(String claveEdo, Poblacion pob) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_poblacion_resumen_descripciones(?,?,?,?)";
    List<PoblacionResumen> data = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(PoblacionResumen.class),
        claveEdo, pob.getAvgpobtot20(), pob.getAvgp0a1120(), pob.getHabitantesVivienda());
    return data.get(0);
  }

  public List<PoblacionPorcentajeEstudios> poblacionPorcentajeEstudios(String isochroneWKT, String claveEdo) {
    String sql = "SELECT * FROM public.___zi_reporte_basico_porcentajes_estudios_poblacion(?,?)";
    List<PoblacionPorcentajeEstudios> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoblacionPorcentajeEstudios.class),
        isochroneWKT, claveEdo);
    return data;
  }

  public Optional<PrecioMetroCuadrado> precioMetroCuadrado(String claveEdo, String claveMun) {
    String sql = "SELECT * FROM public.___zi_precio_tierra_metro_cuadrado(?, ?)";

    List<PrecioMetroCuadrado> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PrecioMetroCuadrado.class), claveEdo,
        claveMun);

    if (data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(data.get(0));
  }

  public List<PoisCategory> poisCategories() {
    String sql = """
        SELECT nivel_3 AS key, nombre_espanol AS name, icon
        FROM tipo_puntos_interes_here_maps
        WHERE basico_gratuito=true
        """;

    List<PoisCategory> data = jdbcTemplate.query(sql,
        BeanPropertyRowMapper.newInstance(PoisCategory.class));
    return data;
  }
}
