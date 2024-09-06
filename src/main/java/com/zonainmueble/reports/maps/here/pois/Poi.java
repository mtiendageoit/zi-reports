package com.zonainmueble.reports.maps.here.pois;

import java.util.*;

import com.zonainmueble.reports.maps.here.Location;

import lombok.Data;

@Data
public class Poi {
  private String id;
  private String title;
  private String language;
  private String resultType;

  private Address address;
  private Location position;
  private List<Location> access;

  private Double distance; // meters
  private List<Category> categories;

  public Category getMainCategory() {
    if (categories != null && !categories.isEmpty()) {
      return categories.stream().filter(item -> item.getPrimary()).findFirst().orElse(null);
    }
    return null;
  }
}
