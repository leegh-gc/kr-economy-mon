package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rank_uatype_sigungu_lease")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RankUatypeSigunguLease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "use_area_type", nullable = false, length = 10)
    private String useAreaType;

    @Column(name = "deal_year", nullable = false, length = 4)
    private String dealYear;

    @Column(name = "rank_type")
    private Integer rankType;

    @Column(name = "rent_gbn", length = 2)
    private String rentGbn;

    @Column(name = "apt_name", length = 100)
    private String aptName;

    @Column(name = "dong_name", length = 50)
    private String dongName;

    @Column(name = "build_year")
    private Integer buildYear;

    @Column(name = "avg_deposit", precision = 15, scale = 2)
    private BigDecimal avgDeposit;

    @Column(name = "min_deposit", precision = 15, scale = 2)
    private BigDecimal minDeposit;

    @Column(name = "max_deposit", precision = 15, scale = 2)
    private BigDecimal maxDeposit;

    @Column(name = "trade_count")
    private Integer dealCount;
}
