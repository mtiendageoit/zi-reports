package com.zonainmueble.reports.factories;

import java.util.*;

import org.springframework.stereotype.Component;

import com.zonainmueble.reports.enums.ReportType;
import com.zonainmueble.reports.services.ReportService;
import com.zonainmueble.reports.services.impl.*;

@Component
public class ReportServiceFactory {
  private final Map<ReportType, ReportService> items;

  private ReportServiceFactory(BasicReportService basic, IntegralReportService integral) {
    items = new HashMap<>();
    items.put(ReportType.BASICO, basic);
    items.put(ReportType.INTEGRAL, integral);
  }

  public ReportService serviceOf(ReportType type) {
    return items.get(type);
  }
}
