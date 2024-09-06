package com.zonainmueble.reports.openai;

import lombok.Getter;

@Getter
public enum OpenAIModel {
  GPT_4("gpt-4o"), GPT_4_MINI("gpt-4o-mini");

  private String id;

  private OpenAIModel(String id) {
    this.id = id;
  }
}