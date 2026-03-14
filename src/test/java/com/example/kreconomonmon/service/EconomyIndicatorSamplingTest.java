package com.example.kreconomonmon.service;

import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EconomyIndicatorSamplingTest {

    @Mock
    private EconomyIndicatorRepository repository;

    @Mock
    private EcosApiService ecosApiService;

    @InjectMocks
    private EconomyIndicatorService service;

    private List<EconomyIndicator> buildIndicators(int count) {
        List<EconomyIndicator> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(EconomyIndicator.builder()
                .statCode("TEST").itemCode("CODE")
                .period(String.format("2020%02d", (i % 12) + 1))
                .value(new BigDecimal(i))
                .updatedAt(LocalDateTime.now())
                .build());
        }
        return list;
    }

    @Test
    void sampleIfNeeded_returns_original_when_within_limit() {
        List<EconomyIndicator> data = buildIndicators(50);
        List<EconomyIndicator> result = service.sampleIfNeeded(data, 100);
        assertThat(result).hasSize(50);
        assertThat(result).isSameAs(data);
    }

    @Test
    void sampleIfNeeded_samples_when_exceeds_limit() {
        List<EconomyIndicator> data = buildIndicators(500);
        List<EconomyIndicator> result = service.sampleIfNeeded(data, 100);
        assertThat(result).hasSize(100);
    }

    @Test
    void sampleIfNeeded_preserves_first_and_last() {
        List<EconomyIndicator> data = buildIndicators(200);
        List<EconomyIndicator> result = service.sampleIfNeeded(data, 50);
        assertThat(result.get(0)).isSameAs(data.get(0));
        assertThat(result).hasSize(50);
    }

    @Test
    void sampleIfNeeded_returns_empty_for_empty_input() {
        List<EconomyIndicator> result = service.sampleIfNeeded(List.of(), 100);
        assertThat(result).isEmpty();
    }
}
