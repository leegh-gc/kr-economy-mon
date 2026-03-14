package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.QStatSigunguYymm;
import com.example.kreconomonmon.entity.StatSigunguYymm;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class StatSigunguYymmRepositoryImpl implements StatSigunguYymmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StatSigunguYymm> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType) {
        QStatSigunguYymm q = QStatSigunguYymm.statSigunguYymm;
        String tenYearsAgo = String.valueOf(LocalDate.now().getYear() - 10) + "01";

        return queryFactory
                .selectFrom(q)
                .where(
                    q.sigunguCode.in(sigunguCodes),
                    q.useAreaType.eq(useAreaType),
                    q.dealYymm.goe(tenYearsAgo)
                )
                .orderBy(q.sigunguCode.asc(), q.dealYymm.asc())
                .fetch();
    }
}
