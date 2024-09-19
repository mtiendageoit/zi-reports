package com.zonainmueble.reports.services.impl;

import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.dto.*;
import com.zonainmueble.reports.models.*;
import com.zonainmueble.reports.services.*;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class IntegralReportService implements ReportService {
  private final String JASPER_REPORT_PATH = "/static/reportes/integral/integral.jasper";

  private final CommonReportService common;
  private final JasperReportService jasper;

  @Override
  public byte[] generateReport(ReportRequest input) {
    Municipio municipio = common.municipioFrom(input.getLatitude(), input.getLongitude());
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("params", reportParams(input, municipio));
    return jasper.generatePdf(JASPER_REPORT_PATH, params);
  }

  protected Map<String, Object> reportParams(ReportRequest input, Municipio municipio) {
    Map<String, Object> params = new HashMap<>();
    return params;
  }

}
