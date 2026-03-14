package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.EconomyIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EconomyIndicatorRepository extends JpaRepository<EconomyIndicator, Long> {

    List<EconomyIndicator> findByStatCodeAndItemCodeOrderByPeriodAsc(String statCode, String itemCode);

    Optional<EconomyIndicator> findByStatCodeAndItemCodeAndPeriod(String statCode, String itemCode, String period);

    /** 특정 statCode + itemCode의 가장 최근 period 반환 */
    @Query("SELECT MAX(e.period) FROM EconomyIndicator e WHERE e.statCode = :statCode AND e.itemCode = :itemCode")
    Optional<String> findLatestPeriod(@Param("statCode") String statCode, @Param("itemCode") String itemCode);
}
