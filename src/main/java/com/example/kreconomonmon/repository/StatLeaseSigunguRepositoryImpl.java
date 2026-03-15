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
    public List<StatLeaseSigungu> findByCodesAndAreaType(List<String> sigunguCodes, String useAreaType, int years) {
        QStatLeaseSigungu q = QStatLeaseSigungu.statLeaseSigungu;
        int currentYear = LocalDate.now().getYear();
        String startYymm = years == 0
                ? currentYear + "01"
                : (currentYear - years) + "01";

        return queryFactory
                .selectFrom(q)
                .where(
                    q.sigunguCode.in(sigunguCodes),
                    q.useAreaType.eq(useAreaType),
                    q.rentGbn.eq("0"),
                    q.dealYymm.goe(startYymm)
                )
                .orderBy(q.sigunguCode.asc(), q.dealYymm.asc())
                .fetch();
    }
}
