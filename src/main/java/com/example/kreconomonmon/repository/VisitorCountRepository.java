package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.VisitorCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitorCountRepository extends JpaRepository<VisitorCount, LocalDate> {

    Optional<VisitorCount> findByVisitDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(v.count), 0) FROM VisitorCount v")
    long sumAllCounts();
}
