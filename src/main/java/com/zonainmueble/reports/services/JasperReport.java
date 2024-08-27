package com.zonainmueble.reports.services;

import java.io.*;
import java.util.*;

import org.springframework.stereotype.Service;

import com.zonainmueble.reports.exceptions.ReportException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;

@Slf4j
@Service
public class JasperReport {

  public byte[] generatePdf(String sourceFileName, Map<String, Object> params) {
    try {
      params.put("REPORT_CLASS_LOADER", getClass().getClassLoader());
      InputStream stream = getClass().getResourceAsStream(sourceFileName);
      JasperPrint jasperPrint = JasperFillManager.fillReport(stream, params, new JREmptyDataSource());
      return JasperExportManager.exportReportToPdf(jasperPrint);
    } catch (JRException e) {
      log.error(e.getMessage(), e);
      log.error("sourceFileName: {}", sourceFileName);
      log.error("params: {}", params);
      throw new ReportException("PDF_REPORT_ERROR",
          "An error occurred while generating the PDF report. Please try again later or contact support if the issue persists");
    }
  }
}
