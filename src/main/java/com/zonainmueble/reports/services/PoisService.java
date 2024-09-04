package com.zonainmueble.reports.services;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.enums.PoiType;

@Service
public class PoisService {

  public PoisResponse pois() {
    List<Poi> pois = new ArrayList<>();
    pois.add(new Poi(8, PoiType.ATM));
    pois.add(new Poi(5, PoiType.CONVENIENCE_STORE));
    return new PoisResponse(pois);
  }
}
