package com.zonainmueble.reports.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ReportException.class)
  public ResponseEntity<ErrorResponse> handleReportException(ReportException ex) {
    return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
