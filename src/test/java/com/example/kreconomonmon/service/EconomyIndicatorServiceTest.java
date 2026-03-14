package com.example.kreconomonmon.service;

import com.example.kreconomonmon.dto.EcosApiResponse;
import com.example.kreconomonmon.entity.EconomyIndicator;
import com.example.kreconomonmon.repository.EconomyIndicatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EconomyIndicatorServiceTest {

    @Mock
    private EconomyIndicatorRepository repository;

    @Mock
    private EcosApiService ecosApiService;

    @InjectMocks
    private EconomyIndicatorService service;

    @Test
    void fetchFromDb_whenDataExists() {
        List<EconomyIndicator> dbData = List.of(
                EconomyIndicator.builder()
                        .statCode("722Y001").itemCode("0101000")
                        .period("202301").value(new BigDecimal("3.5")).build()
        );
        when(repository.findByStatCodeAndItemCodeOrderByPeriodAsc("722Y001", "0101000"))
                .thenReturn(dbData);

        List<EconomyIndicator> result = service.getIndicators("722Y001", "M", "0101000");

        assertThat(result).hasSize(1);
        verify(ecosApiService, never()).fetchStatistic(any(), any(), any());
    }

    @Test
    void fetchFromEcos_whenDbEmpty() {
        when(repository.findByStatCodeAndItemCodeOrderByPeriodAsc("722Y001", "0101000"))
                .thenReturn(List.of());

        EcosApiResponse.Row row = mock(EcosApiResponse.Row.class);
        when(row.getTime()).thenReturn("202301");
        when(row.getDataValue()).thenReturn("3.5");
        when(row.getStatCode()).thenReturn("722Y001");
        when(row.getItemCode1()).thenReturn("0101000");
        when(ecosApiService.fetchStatistic("722Y001", "M", "0101000"))
                .thenReturn(List.of(row));

        EconomyIndicator savedIndicator = EconomyIndicator.builder()
                .statCode("722Y001").itemCode("0101000")
                .period("202301").value(new BigDecimal("3.5")).build();
        when(repository.save(any(EconomyIndicator.class))).thenReturn(savedIndicator);

        List<EconomyIndicator> result = service.getIndicators("722Y001", "M", "0101000");

        assertThat(result).hasSize(1);
        verify(ecosApiService).fetchStatistic("722Y001", "M", "0101000");
        verify(repository, atLeastOnce()).save(any(EconomyIndicator.class));
    }
}
