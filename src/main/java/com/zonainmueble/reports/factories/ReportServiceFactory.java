package com.zonainmueble.reports.factories;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.zonainmueble.reports.enums.ReportType;
import com.zonainmueble.reports.services.ReportService;
import com.zonainmueble.reports.services.impl.BasicReportService;

@Component
public class ReportServiceFactory {
  private final Map<ReportType, ReportService> items;

  private ReportServiceFactory(BasicReportService basic) {
    items = new HashMap<>();
    items.put(ReportType.BASIC, basic);
  }

  public ReportService serviceOf(ReportType type) {
    return items.get(type);
  }
}
