package com.example.kreconomonmon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GeminiApiServiceTest {

    private final GeminiApiService service = new GeminiApiService(
        "test-api-key",
        "gemini-3-flash-preview",
        "imagen-4.0-fast-generate-001"
    );

    @Test
    void buildRequestBody_contains_prompt() {
        String requestBody = service.buildRequestBody("경제 분석 요청 프롬프트");

        assertThat(requestBody).contains("경제 분석 요청 프롬프트");
        assertThat(requestBody).contains("contents");
        assertThat(requestBody).contains("parts");
    }

    @Test
    void buildRequestBody_handles_special_characters() {
        String requestBody = service.buildRequestBody("탭\t문자와 \"따옴표\" 테스트");

        assertThat(requestBody).contains("탭");
        assertThat(requestBody).contains("따옴표");
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

    @Test
    void buildImageRequestBody_contains_instances_and_prompt() {
        String requestBody = service.buildImageRequestBody("컷툰 생성 프롬프트");

        assertThat(requestBody).contains("컷툰 생성 프롬프트");
        assertThat(requestBody).contains("instances");
        assertThat(requestBody).contains("prompt");
        assertThat(requestBody).contains("parameters");
    }

    @Test
    void extractImageFromResponse_parses_predictions() {
        String mockResponse = """
            {
              "predictions": [
                {
                  "bytesBase64Encoded": "base64encodedimagedata=="
                }
              ]
            }
            """;

        String extracted = service.extractImageFromResponse(mockResponse);

        assertThat(extracted).isEqualTo("base64encodedimagedata==");
    }

    @Test
    void extractImageFromResponse_returns_empty_when_no_predictions() {
        String mockResponse = """
            {
              "predictions": []
            }
            """;

        String extracted = service.extractImageFromResponse(mockResponse);

        assertThat(extracted).isEmpty();
    }

    @Test
    void extractImageFromResponse_returns_empty_on_malformed_json() {
        String extracted = service.extractImageFromResponse("{ invalid }");
        assertThat(extracted).isEmpty();
    }
}
