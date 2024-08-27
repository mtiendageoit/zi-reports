package com.zonainmueble.reports.factories;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.zonainmueble.reports.enums.ReportType;
import com.zonainmueble.reports.services.BasicReportService;
import com.zonainmueble.reports.services.IReportService;

@Component
public class ReportServiceFactory {
  private final Map<ReportType, IReportService> items;

  private ReportServiceFactory(BasicReportService basic) {
    items = new HashMap<>();
    items.put(ReportType.BASIC, basic);
  }

  public IReportService serviceOf(ReportType type) {
    return items.get(type);
  }
}
