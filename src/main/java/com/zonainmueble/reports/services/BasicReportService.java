package com.zonainmueble.reports.services;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.ReportRequestDto;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BasicReportService implements IReportService {
  private final String JASPER_REPORT_PATH = "/static/reports/basic/basic.jasper";

  private final JasperReport jasperReport;

  @Override
  public byte[] generateReport(ReportRequestDto input) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input));

    return jasperReport.generatePdf(JASPER_REPORT_PATH, params);
  }

  private Map<String, Object> reportParams(ReportRequestDto input) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("address", input.getAddress());
    return params;
  }
}
