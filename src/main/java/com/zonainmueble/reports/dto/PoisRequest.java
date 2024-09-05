package com.zonainmueble.reports.dto;

import java.util.*;
import lombok.Data;

@Data
public class PoisRequest {
  private Coordinate center;
  private Extent boundingBox;
  private List<String> categories;
  private Integer limit;
}
