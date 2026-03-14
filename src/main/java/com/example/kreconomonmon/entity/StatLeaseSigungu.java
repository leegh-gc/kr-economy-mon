package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stat_lease_sigungu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StatLeaseSigungu {

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

    @Column(name = "rent_gbn", length = 2)
    private String rentGbn;

    @Column(name = "avg_deposit", precision = 15, scale = 2)
    private BigDecimal avgDeposit;

    @Column(name = "min_deposit", precision = 15, scale = 2)
    private BigDecimal minDeposit;

    @Column(name = "max_deposit", precision = 15, scale = 2)
    private BigDecimal maxDeposit;

    @Column(name = "deal_count")
    private Integer dealCount;
}
