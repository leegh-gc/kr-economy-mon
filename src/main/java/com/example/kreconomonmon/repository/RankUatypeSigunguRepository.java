package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.RankUatypeSigungu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankUatypeSigunguRepository extends JpaRepository<RankUatypeSigungu, Long> {

    List<RankUatypeSigungu> findBySigunguCodeAndUseAreaTypeAndDealYearAndRankTypeOrderByAvgPriceDesc(
        String sigunguCode,
        String useAreaType,
        String dealYear,
        Integer rankType,
        Pageable pageable
    );
}
