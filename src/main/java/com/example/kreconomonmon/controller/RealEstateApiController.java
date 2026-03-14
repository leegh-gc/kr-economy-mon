package com.example.kreconomonmon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/real-estate")
public class RealEstateApiController {

    @GetMapping("/kb-index")
    public ResponseEntity<Map<String, Object>> getKbIndex() {
        // Phase 3에서 ECOS API + DB 연동으로 교체 예정
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "message", "KB 지수 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "region", region,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/lease")
    public ResponseEntity<Map<String, Object>> getLease(
            @RequestParam String region,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "region", region,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/top5/trade")
    public ResponseEntity<Map<String, Object>> getTop5Trade(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "sigunguCode", sigunguCode,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }

    @GetMapping("/top5/lease")
    public ResponseEntity<Map<String, Object>> getTop5Lease(
            @RequestParam String sigunguCode,
            @RequestParam(defaultValue = "UA04") String areaType) {
        return ResponseEntity.ok(Map.of(
            "status", "mock",
            "sigunguCode", sigunguCode,
            "areaType", areaType,
            "message", "DB 연동 예정 (Phase 3)"
        ));
    }
}
