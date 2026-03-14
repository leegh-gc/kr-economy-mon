package com.example.kreconomonmon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class GeminiApiService {

    private static final String GEMINI_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private static final String GEMINI_IMAGE_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiApiService(@Value("${GEMINI_API_KEY:}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY가 설정되지 않았습니다. 분석을 건너뜁니다.");
            throw new IllegalStateException("Gemini API 키가 설정되지 않았습니다.");
        }

        String requestBody = buildRequestBody(prompt);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API 오류: status={}, body={}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API 호출 실패: HTTP " + response.statusCode());
            }

            return extractTextFromResponse(response.body());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API 호출 중 예외 발생", e);
            throw new RuntimeException("Gemini API 호출 중 오류가 발생했습니다.", e);
        }
    }

    String buildRequestBody(String prompt) {
        String escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");

        return """
            {
              "contents": [
                {
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ],
              "generationConfig": {
                "maxOutputTokens": 1024,
                "temperature": 0.7
              }
            }
            """.formatted(escapedPrompt);
    }

    String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText("");
        } catch (Exception e) {
            log.warn("Gemini 응답 파싱 실패: {}", e.getMessage());
            return "";
        }
    }

    public String generateImage(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY가 설정되지 않았습니다. 이미지 생성을 건너뜁니다.");
            throw new IllegalStateException("Gemini API 키가 설정되지 않았습니다.");
        }

        String requestBody = buildImageRequestBody(prompt);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_IMAGE_API_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini 이미지 API 오류: status={}", response.statusCode());
                throw new RuntimeException("Gemini 이미지 API 호출 실패: HTTP " + response.statusCode());
            }

            return extractImageFromResponse(response.body());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini 이미지 API 호출 중 예외 발생", e);
            throw new RuntimeException("Gemini 이미지 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    String buildImageRequestBody(String prompt) {
        String escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");

        return """
            {
              "contents": [
                {
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ],
              "generationConfig": {
                "responseModalities": ["IMAGE", "TEXT"]
              }
            }
            """.formatted(escapedPrompt);
    }

    String extractImageFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").get(0)
                .path("content").path("parts");
            for (JsonNode part : parts) {
                JsonNode inlineData = part.path("inlineData");
                if (!inlineData.isMissingNode()) {
                    return inlineData.path("data").asText("");
                }
            }
            log.warn("Gemini 응답에 이미지 데이터가 없습니다.");
            return "";
        } catch (Exception e) {
            log.warn("Gemini 이미지 응답 파싱 실패: {}", e.getMessage());
            return "";
        }
    }
}
