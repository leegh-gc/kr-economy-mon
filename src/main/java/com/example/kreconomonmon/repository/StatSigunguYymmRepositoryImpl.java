package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.QStatSigunguYymm;
import com.example.kreconomonmon.entity.StatSigunguYymm;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class StatSigunguYymmRepositoryImpl implements StatSigunguYymmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StatSigunguYymmRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<StatSigunguYymm> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType, int years) {
        QStatSigunguYymm q = QStatSigunguYymm.statSigunguYymm;
        int currentYear = LocalDate.now().getYear();
        String startYymm = years == 0
                ? currentYear + "01"
                : (currentYear - years) + "01";

        return queryFactory
                .selectFrom(q)
                .where(
                    q.sigunguCode.in(sigunguCodes),
                    q.useAreaType.eq(useAreaType),
                    q.dealYymm.goe(startYymm)
                )
                .orderBy(q.sigunguCode.asc(), q.dealYymm.asc())
                .fetch();
    }
}
