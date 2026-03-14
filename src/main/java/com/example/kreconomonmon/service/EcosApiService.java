package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EcosApiService {

    @Value("${app.ecos.api-key}")
    private String apiKey;

    @Value("${app.ecos.base-url}")
    private String baseUrl;

    private static final int MAX_ROWS = 500;
    private static final int MAX_RETRY = 3;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ECOS API 호출. statCode, cycle, itemCode1을 받아 조회 기간을 자동 계산 후 데이터 반환.
     *
     * @param statCode  통계코드 (예: "722Y001")
     * @param cycle     주기 (A/Q/M/D)
     * @param itemCode1 항목코드 (예: "0101000")
     * @return 조회 결과 Row 리스트
     */
    public List<EcosApiResponse.Row> fetchStatistic(String statCode, String cycle, String itemCode1) {
        String[] range = buildDateRange(cycle);
        return fetchStatistic(statCode, cycle, itemCode1, range[0], range[1]);
    }

    public List<EcosApiResponse.Row> fetchStatistic(String statCode, String cycle, String itemCode1,
                                                     String startDate, String endDate) {
        String url = String.format(
                "%s/StatisticSearch/%s/json/kr/1/%d/%s/%s/%s/%s/%s",
                baseUrl, apiKey, MAX_ROWS, statCode, cycle, startDate, endDate, itemCode1
        );

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.warn("ECOS API HTTP {}: attempt {}/{}", response.statusCode(), attempt, MAX_RETRY);
                    continue;
                }

                EcosApiResponse parsed = objectMapper.readValue(response.body(), EcosApiResponse.class);

                if (parsed.getStatisticSearch() == null || parsed.getStatisticSearch().getRows() == null) {
                    log.warn("ECOS API 응답에 데이터 없음 (statCode={}, attempt={})", statCode, attempt);
                    return List.of();
                }

                return parsed.getStatisticSearch().getRows();

            } catch (Exception e) {
                log.warn("ECOS API 호출 오류 (attempt {}/{}): {}", attempt, MAX_RETRY, e.getMessage());
                if (attempt == MAX_RETRY) {
                    log.error("ECOS API 최대 재시도 초과 (statCode={})", statCode, e);
                }
            }
        }

        return List.of();
    }

    /**
     * 주기(cycle)에 따라 조회 시작/종료 날짜 문자열 배열 반환. [startDate, endDate]
     */
    String[] buildDateRange(String cycle) {
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        return switch (cycle.toUpperCase()) {
            case "A" -> new String[]{
                    String.valueOf(currentYear - 30),
                    String.valueOf(currentYear - 1)
            };
            case "Q" -> new String[]{
                    (currentYear - 10) + "Q1",
                    (currentYear - 1) + "Q4"
            };
            case "M" -> new String[]{
                    (currentYear - 10) + "01",
                    (currentYear - 1) + "12"
            };
            case "D" -> new String[]{
                    (currentYear - 1) + "0101",
                    today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            };
            default -> throw new IllegalArgumentException("지원하지 않는 주기: " + cycle);
        };
    }
}
