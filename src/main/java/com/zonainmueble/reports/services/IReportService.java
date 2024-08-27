package com.zonainmueble.reports.services;

import com.zonainmueble.reports.dto.ReportRequestDto;

public interface IReportService {
  byte[] generateReport(ReportRequestDto request);
}
