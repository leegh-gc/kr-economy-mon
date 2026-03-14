package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.StatLeaseSigungu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatLeaseSigunguRepository
        extends JpaRepository<StatLeaseSigungu, Long>, StatLeaseSigunguRepositoryCustom {
}
