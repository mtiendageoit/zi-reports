package com.zonainmueble.reports.openai;

import java.util.List;
import lombok.Data;

@Data
public class CompletionResponse {
  private List<Choice> choices;
}
