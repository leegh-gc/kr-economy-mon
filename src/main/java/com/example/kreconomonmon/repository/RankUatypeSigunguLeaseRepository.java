package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.RankUatypeSigunguLease;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankUatypeSigunguLeaseRepository extends JpaRepository<RankUatypeSigunguLease, Long> {

    List<RankUatypeSigunguLease> findBySigunguCodeInAndUseAreaTypeAndDealYearAndRankTypeAndRentGbnOrderByAvgDepositDesc(
        List<String> sigunguCode,
        String useAreaType,
        String dealYear,
        Integer rankType,
        String rentGbn,
        Pageable pageable
    );
}
