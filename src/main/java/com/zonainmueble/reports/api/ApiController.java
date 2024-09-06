package com.zonainmueble.reports.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.zonainmueble.reports.dto.ReportRequest;
import com.zonainmueble.reports.enums.ReportType;
import com.zonainmueble.reports.factories.ReportServiceFactory;
import com.zonainmueble.reports.services.ReportService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/reports")
public class ApiController {
  private final ReportServiceFactory serviceFactory;

  @PostMapping
  public ResponseEntity<byte[]> report(@RequestParam ReportType type, @Valid @RequestBody ReportRequest input) {
    log.info("Processing report type: {}, input: {}", type, input);

    ReportService service = serviceFactory.serviceOf(type);
    byte[] report = service.generateReport(input);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "report.pdf");
    return new ResponseEntity<>(report, headers, HttpStatus.OK);
  }
}
