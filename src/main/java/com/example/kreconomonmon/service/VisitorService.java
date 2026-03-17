package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.VisitorCount;
import com.example.kreconomonmon.repository.VisitorCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorCountRepository visitorCountRepository;

    @Transactional
    public void recordVisit() {
        LocalDate today = LocalDate.now();
        VisitorCount vc = visitorCountRepository.findByVisitDate(today)
                .orElse(VisitorCount.builder().visitDate(today).count(0L).build());
        vc.increment();
        visitorCountRepository.save(vc);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCounts() {
        LocalDate today = LocalDate.now();
        long todayCount = visitorCountRepository.findByVisitDate(today)
                .map(VisitorCount::getCount)
                .orElse(0L);
        long total = visitorCountRepository.sumAllCounts();
        return Map.of("today", todayCount, "total", total);
    }
}
