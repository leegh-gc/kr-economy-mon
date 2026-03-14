package com.example.kreconomonmon.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class Top5Response {
    private String aptName;
    private String dongName;
    private Integer buildYear;
    private BigDecimal avgPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer dealCount;
}
