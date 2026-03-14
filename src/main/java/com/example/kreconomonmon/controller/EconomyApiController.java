package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/economy")
@RequiredArgsConstructor
public class EconomyApiController {

    private final EconomyIndicatorService economyIndicatorService;

    // ── 섹션1: 금리 ──────────────────────────────────────
    @GetMapping("/interest-rate")
    public ResponseEntity<ChartDataResponse> getInterestRate() {
        List<EconomyIndicator> baseRate =
                economyIndicatorService.getIndicators("722Y001", "M", "0101000");
        List<EconomyIndicator> bond3Y =
                economyIndicatorService.getIndicators("817Y002", "D", "010200000");
        List<EconomyIndicator> bond10Y =
                economyIndicatorService.getIndicators("817Y002", "D", "010210000");

        List<String> labels = baseRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(baseRate, "기준금리 (%)", "#dc3545", null),
                        toDataset(bond3Y,   "국고채 3년 (%)", "#007bff", null),
                        toDataset(bond10Y,  "국고채 10년 (%)", "#28a745", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션2: GDP ───────────────────────────────────────
    @GetMapping("/gdp")
    public ResponseEntity<ChartDataResponse> getGdp() {
        List<EconomyIndicator> growthRate =
                economyIndicatorService.getIndicators("902Y015", "Q", "KOR");
        List<EconomyIndicator> gdpPerCapita =
                economyIndicatorService.getIndicators("902Y018", "A", "KOR");

        List<String> labels = growthRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(growthRate,   "GDP 성장률 (%)", "#007bff", null),
                        toDataset(gdpPerCapita, "1인당 GDP (달러)", "#6c757d", "y1")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션3: 환율 ──────────────────────────────────────
    @GetMapping("/exchange-rate")
    public ResponseEntity<ChartDataResponse> getExchangeRate() {
        List<EconomyIndicator> usd =
                economyIndicatorService.getIndicators("731Y001", "D", "0000001");
        List<EconomyIndicator> eur =
                economyIndicatorService.getIndicators("731Y001", "D", "0000003");
        List<EconomyIndicator> jpy =
                economyIndicatorService.getIndicators("731Y001", "D", "0000002");
        List<EconomyIndicator> cny =
                economyIndicatorService.getIndicators("731Y001", "D", "0000053");

        List<String> labels = usd.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(usd, "USD (원)", "#007bff", "y"),
                        toDataset(eur, "EUR (원)", "#fd7e14", "y"),
                        toDataset(jpy, "JPY 100엔 (원)", "#dc3545", "y1"),
                        toDataset(cny, "CNY (원)", "#28a745", "y")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션4: 물가 ──────────────────────────────────────
    @GetMapping("/price-index")
    public ResponseEntity<ChartDataResponse> getPriceIndex() {
        List<EconomyIndicator> cpi =
                economyIndicatorService.getIndicators("901Y009", "M", "0");
        List<EconomyIndicator> ppi =
                economyIndicatorService.getIndicators("404Y014", "M", "*AA");

        List<String> labels = cpi.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(cpi, "소비자물가지수 (CPI)", "#007bff", null),
                        toDataset(ppi, "생산자물가지수 (PPI)", "#fd7e14", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 나머지 엔드포인트 (Sprint 3 구현 예정, mock 유지) ──
    @GetMapping("/trade")
    public ResponseEntity<Map<String, Object>> getTrade() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/employment")
    public ResponseEntity<Map<String, Object>> getEmployment() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/liquidity")
    public ResponseEntity<Map<String, Object>> getLiquidity() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    @GetMapping("/population")
    public ResponseEntity<Map<String, Object>> getPopulation() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "Sprint 3 구현 예정"));
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────
    private ChartDataResponse.Dataset toDataset(
            List<EconomyIndicator> indicators,
            String label,
            String color,
            String yAxisID) {

        List<Double> data = indicators.stream()
                .map(e -> e.getValue() != null ? e.getValue().doubleValue() : null)
                .collect(Collectors.toList());

        ChartDataResponse.Dataset.DatasetBuilder builder = ChartDataResponse.Dataset.builder()
                .label(label)
                .data(data)
                .borderColor(color)
                .backgroundColor(color)
                .fill(false);

        if (yAxisID != null) {
            builder.yAxisID(yAxisID);
        }

        return builder.build();
    }
}
