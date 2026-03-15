package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.StatSigunguYymm;
import java.util.List;

public interface StatSigunguYymmRepositoryCustom {
    List<StatSigunguYymm> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType, int years);
}
