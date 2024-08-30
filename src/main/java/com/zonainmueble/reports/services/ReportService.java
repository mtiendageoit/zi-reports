package com.zonainmueble.reports.services;

import com.zonainmueble.reports.dto.ReportRequest;

public interface ReportService {
  byte[] generateReport(ReportRequest request);
}
