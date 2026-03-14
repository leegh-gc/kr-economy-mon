package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stat_sigungu_yymm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StatSigunguYymm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "use_area_type", nullable = false, length = 10)
    private String useAreaType;

    @Column(name = "deal_yymm", nullable = false, length = 6)
    private String dealYymm;

    @Column(name = "avg_price", precision = 15, scale = 2)
    private BigDecimal avgPrice;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "deal_count")
    private Integer dealCount;
}
