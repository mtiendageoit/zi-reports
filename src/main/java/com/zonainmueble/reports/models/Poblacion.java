package com.zonainmueble.reports.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Poblacion {
  @Id
  private int id;
  private Double pobtot20;
  private Double pobtot10;
  private Double ninos;
  private Double hombres;
  private Double mujeres;
  private Double pHombres;
  private Double pMujeres;
  private Double vivtot20;
  private Double tothog20;
  private Double tvivpah20;
  private Double ocuvivp20;
  private Double habitantesVivienda;
  private Double vph1dor20;
  private Double vph2ymd20;
  private Double gentrificacion;

  private Double avgpobtot20;
  private Double avgp0a1120;
}