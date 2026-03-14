package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.service.EconomyIndicatorScheduler;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final EconomyIndicatorScheduler economyIndicatorScheduler;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh() {
        economyIndicatorScheduler.refreshAllIndicators();
        return ResponseEntity.ok(Map.of("status", "ok", "message", "경제 지표 갱신 완료"));
    }

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

    // ── 섹션2a: GDP 성장률 ──────────────────────────────
    @GetMapping("/gdp")
    public ResponseEntity<ChartDataResponse> getGdp() {
        List<EconomyIndicator> growthRate =
                economyIndicatorService.getIndicators("902Y015", "Q", "KOR");

        List<String> labels = growthRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(growthRate, "GDP 성장률 (%)", "#007bff", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션2b: 1인당 국민소득 ────────────────────────────
    @GetMapping("/gdp-income")
    public ResponseEntity<ChartDataResponse> getGdpIncome() {
        List<EconomyIndicator> gdpPerCapita =
                economyIndicatorService.getIndicators("902Y018", "A", "KOR");

        List<String> labels = gdpPerCapita.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(gdpPerCapita, "1인당 GDP (달러)", "#6c757d", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션3: 환율 ──────────────────────────────────────
    @GetMapping("/exchange-rate")
    public ResponseEntity<ChartDataResponse> getExchangeRate() {
        List<EconomyIndicator> usd =
                economyIndicatorService.getIndicators("731Y001", "D", "0000001");
        List<EconomyIndicator> jpy =
                economyIndicatorService.getIndicators("731Y001", "D", "0000002");

        List<String> labels = usd.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(usd, "USD (원)", "#007bff", "y"),
                        toDataset(jpy, "JPY 100엔 (원)", "#dc3545", "y1")
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

    // ── 섹션5: 무역/경상수지 ────────────────────────────────
    @GetMapping("/trade")
    public ResponseEntity<ChartDataResponse> getTrade() {
        List<EconomyIndicator> currentAccount =
                economyIndicatorService.getIndicators("301Y013", "M", "000000");
        List<EconomyIndicator> exports =
                economyIndicatorService.getIndicators("901Y118", "M", "T002");
        List<EconomyIndicator> imports =
                economyIndicatorService.getIndicators("901Y118", "M", "T004");

        List<String> labels = currentAccount.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(currentAccount, "경상수지 (백만달러)", "#007bff", null),
                        toDataset(exports,        "수출금액 (백만달러)", "#28a745", "y1"),
                        toDataset(imports,        "수입금액 (백만달러)", "#dc3545", "y1")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션6: 고용/경기 ─────────────────────────────────
    @GetMapping("/employment")
    public ResponseEntity<ChartDataResponse> getEmployment() {
        List<EconomyIndicator> unemploymentRate =
                economyIndicatorService.getIndicators("901Y027", "M", "I61BC");
        List<EconomyIndicator> employed =
                economyIndicatorService.getIndicators("901Y027", "M", "I61BA");

        List<String> labels = unemploymentRate.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(unemploymentRate, "실업률 (%)",     "#dc3545", "y"),
                        toDataset(employed,         "취업자수 (천명)", "#007bff", "y1")
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션7a: 통화량 (M2) ──────────────────────────────
    @GetMapping("/liquidity")
    public ResponseEntity<ChartDataResponse> getLiquidity() {
        List<EconomyIndicator> m2 =
                economyIndicatorService.getIndicators("902Y005", "M", "KR");

        List<String> labels = m2.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(m2, "M2 광의통화 (조원)", "#6610f2", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션7b: 외환보유액 ───────────────────────────────
    @GetMapping("/forex-reserve")
    public ResponseEntity<ChartDataResponse> getForexReserve() {
        List<EconomyIndicator> reserveKr =
                economyIndicatorService.getIndicators("902Y014", "M", "KR");
        List<EconomyIndicator> reserveJp =
                economyIndicatorService.getIndicators("902Y014", "M", "JP");

        List<String> labels = reserveKr.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(reserveKr, "한국 (억달러)", "#007bff", null),
                        toDataset(reserveJp, "일본 (억달러)", "#fd7e14", null)
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    // ── 섹션8: 인구/출산율/고령화 ────────────────────────
    @GetMapping("/population")
    public ResponseEntity<ChartDataResponse> getPopulation() {
        List<EconomyIndicator> popTotal =
                economyIndicatorService.getIndicators("901Y028", "A", "I35A");
        List<EconomyIndicator> elderRatio =
                economyIndicatorService.getIndicators("901Y028", "A", "I35D");
        List<EconomyIndicator> birthRate =
                economyIndicatorService.getIndicators("901Y028", "A", "I35E");

        List<String> labels = popTotal.stream()
                .map(EconomyIndicator::getPeriod)
                .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
                .labels(labels)
                .datasets(List.of(
                        toDataset(popTotal,   "총인구 (천명)",     "#007bff", "y"),
                        toDataset(elderRatio, "고령인구비율 (%)", "#fd7e14", "y1"),
                        toDataset(birthRate,  "합계출산율",        "#28a745", "y1")
                ))
                .build();

        return ResponseEntity.ok(response);
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
