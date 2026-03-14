package com.example.kreconomonmon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GeminiApiServiceTest {

    private final GeminiApiService service = new GeminiApiService("test-api-key");

    @Test
    void buildRequestBody_contains_prompt() {
        String requestBody = service.buildRequestBody("경제 분석 요청 프롬프트");

        assertThat(requestBody).contains("경제 분석 요청 프롬프트");
        assertThat(requestBody).contains("contents");
        assertThat(requestBody).contains("parts");
    }

    @Test
    void extractTextFromResponse_parses_valid_json() {
        String mockResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "분석 결과 텍스트입니다." }
                    ]
                  }
                }
              ]
            }
            """;

        String extracted = service.extractTextFromResponse(mockResponse);

        assertThat(extracted).isEqualTo("분석 결과 텍스트입니다.");
    }

    @Test
    void extractTextFromResponse_returns_empty_on_malformed_json() {
        String extracted = service.extractTextFromResponse("{ invalid json }");
        assertThat(extracted).isEmpty();
    }
}
