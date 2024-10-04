package com.zonainmueble.reports.models;

import lombok.Data;

@Data
public class PoblacionPorcentajeEstudios {
  private Double pobtotEdo;
  private Double pobtotIsocrona;
  private Double pobtotIsocronaRango;
  private Double grpesc20Isocrona;
  private Double pParticipacionEdo;
  private Double pParticipacionRango;
  private String descripcion;
  private String descripcionCorta;
  private String color;
}
