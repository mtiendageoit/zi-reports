package com.zonainmueble.reports.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {
  @Value("${app.isochrone.buffer.meters}")
  private Double isochroneBufferMeters;

  @Value("${apis.openai.completions.system.message}")
  private String completionsSystemMessage;
}
