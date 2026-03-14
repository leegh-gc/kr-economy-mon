package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.GeminiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiApiController {

    private final GeminiAnalysisService geminiAnalysisService;

    @GetMapping("/economy-analysis")
    public ResponseEntity<Map<String, Object>> getEconomyAnalysis() {
        return ResponseEntity.ok(geminiAnalysisService.getEconomyAnalysis());
    }

    @GetMapping("/realestate-analysis")
    public ResponseEntity<Map<String, Object>> getRealEstateAnalysis() {
        return ResponseEntity.ok(geminiAnalysisService.getRealEstateAnalysis());
    }

    @GetMapping("/economy-cartoon")
    public ResponseEntity<Map<String, Object>> getEconomyCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Sprint 5)"
        ));
    }

    @GetMapping("/realestate-cartoon")
    public ResponseEntity<Map<String, Object>> getRealEstateCartoon() {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "imageUrl", "",
            "message", "Gemini 컷툰 생성 연동 예정 (Sprint 5)"
        ));
    }
}
