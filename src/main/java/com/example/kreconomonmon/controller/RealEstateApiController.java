package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.dto.Top5Response;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.service.EconomyIndicatorService;
import com.example.kreconomonmon.service.RealEstateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/real-estate")
@RequiredArgsConstructor
public class RealEstateApiController {

    private final RealEstateService realEstateService;
    private final EconomyIndicatorService economyIndicatorService;

    @GetMapping("/kb-index")
    public ResponseEntity<ChartDataResponse> getKbIndex() {
        List<EconomyIndicator> tradeIdx =
            economyIndicatorService.getIndicators("901Y062", "M", "P63ACA");
        List<EconomyIndicator> leaseIdx =
            economyIndicatorService.getIndicators("901Y063", "M", "P64ACA");

        List<String> labels = tradeIdx.stream()
            .map(EconomyIndicator::getPeriod)
            .collect(Collectors.toList());

        ChartDataResponse response = ChartDataResponse.builder()
            .labels(labels)
            .datasets(List.of(
                toDataset(tradeIdx, "KB 매매지수", "#007bff"),
                toDataset(leaseIdx, "KB 전세지수", "#28a745")
            ))
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/price")
    public ResponseEntity<ChartDataResponse> getPrice(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType,
            @RequestParam String codes) {

        List<String> sigunguCodes = Arrays.asList(codes.split(","));
        return ResponseEntity.ok(realEstateService.getPriceChartData(sigunguCodes, areaType));
    }

    @GetMapping("/lease")
    public ResponseEntity<ChartDataResponse> getLease(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType,
            @RequestParam String codes) {

        List<String> sigunguCodes = Arrays.asList(codes.split(","));
        return ResponseEntity.ok(realEstateService.getLeaseChartData(sigunguCodes, areaType));
    }

    @GetMapping("/top5/trade")
    public ResponseEntity<List<Top5Response>> getTop5Trade(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(realEstateService.getTop5Trade(sigunguCode, areaType));
    }

    @GetMapping("/top5/lease")
    public ResponseEntity<List<Top5Response>> getTop5Lease(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(realEstateService.getTop5Lease(sigunguCode, areaType));
    }

    private ChartDataResponse.Dataset toDataset(
            List<EconomyIndicator> indicators, String label, String color) {
        List<Double> data = indicators.stream()
            .map(e -> e.getValue() != null ? e.getValue().doubleValue() : null)
            .collect(Collectors.toList());
        return ChartDataResponse.Dataset.builder()
            .label(label).data(data)
            .borderColor(color).backgroundColor(color)
            .fill(false).build();
    }
}
