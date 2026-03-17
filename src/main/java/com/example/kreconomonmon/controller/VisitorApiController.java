package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorApiController {

    private final VisitorService visitorService;

    @GetMapping
    public Map<String, Long> getCounts() {
        return visitorService.getCounts();
    }
}
