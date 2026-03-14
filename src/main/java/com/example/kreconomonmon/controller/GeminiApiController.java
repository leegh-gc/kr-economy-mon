package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.CartoonResponse;
import com.example.kreconomonmon.service.AnalysisCacheService;
import com.example.kreconomonmon.service.GeminiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiApiController {

    private final GeminiAnalysisService geminiAnalysisService;
    private final AnalysisCacheService analysisCacheService;

    @GetMapping("/economy-analysis")
    public ResponseEntity<Map<String, Object>> getEconomyAnalysis() {
        return ResponseEntity.ok(geminiAnalysisService.getEconomyAnalysis());
    }

    @GetMapping("/realestate-analysis")
    public ResponseEntity<Map<String, Object>> getRealEstateAnalysis() {
        return ResponseEntity.ok(geminiAnalysisService.getRealEstateAnalysis());
    }

    @GetMapping("/economy-cartoon")
    public ResponseEntity<CartoonResponse> getEconomyCartoon() {
        Map<String, Object> result = geminiAnalysisService.getEconomyCartoon();
        CartoonResponse response = new CartoonResponse(
            (String) result.get("status"),
            (String) result.get("imageData"),
            Boolean.TRUE.equals(result.get("cached"))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/realestate-cartoon")
    public ResponseEntity<CartoonResponse> getRealEstateCartoon() {
        Map<String, Object> result = geminiAnalysisService.getRealEstateCartoon();
        CartoonResponse response = new CartoonResponse(
            (String) result.get("status"),
            (String) result.get("imageData"),
            Boolean.TRUE.equals(result.get("cached"))
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAll() {
        analysisCacheService.invalidateAll();
        return ResponseEntity.ok(Map.of("status", "ok", "message", "캐시가 초기화되었습니다."));
    }
}
