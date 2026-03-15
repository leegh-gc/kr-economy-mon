package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.CartoonResponse;
import com.example.kreconomonmon.service.AnalysisCacheService;
import com.example.kreconomonmon.service.GeminiAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiApiController {

    private final GeminiAnalysisService geminiAnalysisService;
    private final AnalysisCacheService analysisCacheService;
    private final String adminKey;

    public GeminiApiController(
            GeminiAnalysisService geminiAnalysisService,
            AnalysisCacheService analysisCacheService,
            @Value("${app.admin-key:}") String adminKey) {
        this.geminiAnalysisService = geminiAnalysisService;
        this.analysisCacheService = analysisCacheService;
        this.adminKey = adminKey;
    }

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
    public ResponseEntity<Map<String, Object>> refreshAll(
            @RequestHeader(value = "X-Admin-Key", required = false) String requestAdminKey) {
        if (adminKey != null && !adminKey.isBlank()) {
            if (requestAdminKey == null || !adminKey.equals(requestAdminKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "인증이 필요합니다."));
            }
        }
        analysisCacheService.invalidateAll();
        return ResponseEntity.ok(Map.of("status", "ok", "message", "캐시가 초기화되었습니다."));
    }
}
