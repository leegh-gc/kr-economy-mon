package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.StatSigunguYymm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatSigunguYymmRepository
        extends JpaRepository<StatSigunguYymm, Long>, StatSigunguYymmRepositoryCustom {
}
