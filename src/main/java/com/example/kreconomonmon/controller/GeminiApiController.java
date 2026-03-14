package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiApiController {

    @GetMapping("/economy-analysis")
    public ResponseEntity<Map<String, Object>> getEconomyAnalysis() {
        // Phase 4에서 Gemini API 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "text", "Gemini AI 경제 분석 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/realestate-analysis")
    public ResponseEntity<Map<String, Object>> getRealEstateAnalysis() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "text", "Gemini AI 부동산 분석 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/economy-cartoon")
    public ResponseEntity<Map<String, Object>> getEconomyCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Phase 4)"
        ));
    }

    @GetMapping("/realestate-cartoon")
    public ResponseEntity<Map<String, Object>> getRealEstateCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Phase 4)"
        ));
    }
}
