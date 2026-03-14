package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rank_uatype_sigungu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RankUatypeSigungu {

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

    @Column(name = "apt_name", length = 100)
    private String aptName;

    @Column(name = "dong_name", length = 50)
    private String dongName;

    @Column(name = "build_year")
    private Integer buildYear;

    @Column(name = "avg_price", precision = 15, scale = 2)
    private BigDecimal avgPrice;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "trade_count")
    private Integer dealCount;
}
