package com.zonainmueble.reports.openai;

import java.util.List;
import lombok.Data;

@Data
public class CompletionRequest {
  private String model;
  private List<Message> messages;
}
