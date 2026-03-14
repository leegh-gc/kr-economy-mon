package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.ChartDataResponse;
import com.example.kreconomonmon.entity.StatLeaseSigungu;
import com.example.kreconomonmon.entity.StatSigunguYymm;
import com.example.kreconomonmon.repository.StatLeaseSigunguRepository;
import com.example.kreconomonmon.repository.StatSigunguYymmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RealEstateService {

    private final StatSigunguYymmRepository tradeRepository;
    private final StatLeaseSigunguRepository leaseRepository;

    private static final Map<String, String> DISTRICT_NAMES;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("11680", "강남구"); m.put("11650", "서초구"); m.put("11710", "송파구");
        m.put("11740", "강동구"); m.put("11350", "노원구"); m.put("11200", "성동구");
        m.put("11500", "강서구"); m.put("11560", "영등포구"); m.put("11470", "양천구");
        m.put("11110", "종로구"); m.put("11440", "마포구"); m.put("11170", "용산구");
        DISTRICT_NAMES = Collections.unmodifiableMap(m);
    }

    private static final List<String> COLORS = List.of("#007bff", "#28a745", "#fd7e14");

    private String getDistrictName(String code) {
        return DISTRICT_NAMES.getOrDefault(code, code);
    }

    public ChartDataResponse getPriceChartData(List<String> sigunguCodes, String useAreaType) {
        List<StatSigunguYymm> rows =
            tradeRepository.findByCodesAndAreaType(sigunguCodes, useAreaType);

        List<String> labels = rows.stream()
            .filter(r -> r.getSigunguCode().equals(sigunguCodes.get(0)))
            .map(StatSigunguYymm::getDealYymm)
            .collect(Collectors.toList());

        Map<String, List<StatSigunguYymm>> grouped =
            rows.stream().collect(Collectors.groupingBy(StatSigunguYymm::getSigunguCode));

        List<ChartDataResponse.Dataset> datasets = new ArrayList<>();
        for (int i = 0; i < sigunguCodes.size(); i++) {
            String code = sigunguCodes.get(i);
            String color = COLORS.get(i % COLORS.size());
            List<StatSigunguYymm> group = grouped.getOrDefault(code, List.of());

            List<Double> values = group.stream()
                .map(t -> t.getAvgPrice() != null ? t.getAvgPrice().doubleValue() : null)
                .collect(Collectors.toList());

            datasets.add(ChartDataResponse.Dataset.builder()
                .label(getDistrictName(code))
                .data(values)
                .borderColor(color)
                .backgroundColor(color)
                .fill(false)
                .build());
        }

        return ChartDataResponse.builder().labels(labels).datasets(datasets).build();
    }

    public ChartDataResponse getLeaseChartData(List<String> sigunguCodes, String useAreaType) {
        List<StatLeaseSigungu> rows =
            leaseRepository.findByCodesAndAreaType(sigunguCodes, useAreaType);

        List<String> labels = rows.stream()
            .filter(r -> r.getSigunguCode().equals(sigunguCodes.get(0)))
            .map(StatLeaseSigungu::getDealYymm)
            .collect(Collectors.toList());

        Map<String, List<StatLeaseSigungu>> grouped =
            rows.stream().collect(Collectors.groupingBy(StatLeaseSigungu::getSigunguCode));

        List<ChartDataResponse.Dataset> datasets = new ArrayList<>();
        for (int i = 0; i < sigunguCodes.size(); i++) {
            String code = sigunguCodes.get(i);
            String color = COLORS.get(i % COLORS.size());
            List<StatLeaseSigungu> group = grouped.getOrDefault(code, List.of());

            List<Double> values = group.stream()
                .map(l -> l.getAvgDeposit() != null ? l.getAvgDeposit().doubleValue() : null)
                .collect(Collectors.toList());

            datasets.add(ChartDataResponse.Dataset.builder()
                .label(getDistrictName(code))
                .data(values)
                .borderColor(color)
                .backgroundColor(color)
                .fill(false)
                .build());
        }

        return ChartDataResponse.builder().labels(labels).datasets(datasets).build();
    }
}
