package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.StatLeaseSigungu;
import java.util.List;

public interface StatLeaseSigunguRepositoryCustom {
    List<StatLeaseSigungu> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType, int years);
}
