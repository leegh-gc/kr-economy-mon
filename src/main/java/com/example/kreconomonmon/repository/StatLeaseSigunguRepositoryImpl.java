package com.example.kreconomonmon.repository;

import com.example.kreconomonmon.entity.QStatLeaseSigungu;
import com.example.kreconomonmon.entity.StatLeaseSigungu;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class StatLeaseSigunguRepositoryImpl implements StatLeaseSigunguRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StatLeaseSigunguRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<StatLeaseSigungu> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType) {
        QStatLeaseSigungu q = QStatLeaseSigungu.statLeaseSigungu;
        String tenYearsAgo = String.valueOf(LocalDate.now().getYear() - 10) + "01";

        return queryFactory
                .selectFrom(q)
                .where(
                    q.sigunguCode.in(sigunguCodes),
                    q.useAreaType.eq(useAreaType),
                    q.rentGbn.eq("0"),
                    q.dealYymm.goe(tenYearsAgo)
                )
                .orderBy(q.sigunguCode.asc(), q.dealYymm.asc())
                .fetch();
    }
}
