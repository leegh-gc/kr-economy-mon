package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final VisitorService visitorService;

    @GetMapping("/")
    public String index() {
        visitorService.recordVisit();
        return "index";
    }
}
