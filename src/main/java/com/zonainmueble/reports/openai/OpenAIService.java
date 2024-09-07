package com.zonainmueble.reports.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class OpenAIService {

  @Value("${apis.openai.key}")
  private String key;

  @Value("${apis.openai.completions.url}")
  private String completionsUrl;

  @Value("${apis.openai.completions.model}")
  private String completionsModel;

  private final RestTemplate restTemplate;

  public OpenAIService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String generateCompletion(CompletionRequest input) {
    String url = this.completionsUrl;

    if (!StringUtils.hasText(input.getModel())) {
      input.setModel(completionsModel);
    }

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(this.key);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<CompletionRequest> entity = new HttpEntity<>(input, headers);

      ResponseEntity<CompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
          CompletionResponse.class);

      return response.getBody().getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.error("\n\nurl: {}\ninput: {}\n\n", url, input);
      throw new RuntimeException("Failed to fetch completion from OpenAI");
    }
  }

}
