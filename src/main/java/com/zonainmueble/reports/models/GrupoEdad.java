package com.zonainmueble.reports.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class GrupoEdad {
  @Id
  private int id;
  private Double p0a2f20;
  private Double p3a520;
  private Double p6a1120;
  private Double p12a1420;
  private Double p15a1720;
  private Double p18a2420;
  private Double pob156420;
  private Double pmay6020;
  private Double pob65ma20;
}
