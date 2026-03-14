package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/economy")
public class EconomyApiController {

    @GetMapping("/interest-rate")
    public ResponseEntity<Map<String, Object>> getInterestRate() {
        // Phase 2에서 ECOS API 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "message", "ECOS API 연동 예정 (Phase 2)"
        ));
    }

    @GetMapping("/gdp")
    public ResponseEntity<Map<String, Object>> getGdp() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/price-index")
    public ResponseEntity<Map<String, Object>> getPriceIndex() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/trade")
    public ResponseEntity<Map<String, Object>> getTrade() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/employment")
    public ResponseEntity<Map<String, Object>> getEmployment() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/liquidity")
    public ResponseEntity<Map<String, Object>> getLiquidity() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }

    @GetMapping("/population")
    public ResponseEntity<Map<String, Object>> getPopulation() {
        return ResponseEntity.ok(Map.of("status", "mock", "message", "ECOS API 연동 예정 (Phase 2)"));
    }
}
