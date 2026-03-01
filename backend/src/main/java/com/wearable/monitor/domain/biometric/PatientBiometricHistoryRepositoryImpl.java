package com.wearable.monitor.domain.biometric;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.wearable.monitor.domain.biometric.QPatientBiometricHistory.patientBiometricHistory;
import static com.wearable.monitor.domain.assignment.QPatientDeviceAssignment.patientDeviceAssignment;

@RequiredArgsConstructor
public class PatientBiometricHistoryRepositoryImpl implements PatientBiometricHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PatientBiometricHistory> findByCondition(Long patientId, List<String> itemCodes,
                                                          LocalDateTime start, LocalDateTime end,
                                                          Pageable pageable) {
        var query = queryFactory
                .selectFrom(patientBiometricHistory)
                .join(patientBiometricHistory.assignment, patientDeviceAssignment).fetchJoin()
                .where(
                        patientIdEq(patientId),
                        itemCodesIn(itemCodes),
                        measuredAtGoe(start),
                        measuredAtLoe(end)
                )
                .orderBy(patientBiometricHistory.measuredAt.desc());

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }

        List<PatientBiometricHistory> content = query.fetch();

        Long total = queryFactory
                .select(patientBiometricHistory.count())
                .from(patientBiometricHistory)
                .join(patientBiometricHistory.assignment, patientDeviceAssignment)
                .where(
                        patientIdEq(patientId),
                        itemCodesIn(itemCodes),
                        measuredAtGoe(start),
                        measuredAtLoe(end)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<DailySummaryDto> findDailySummary(List<Long> patientIds, List<String> itemCodes,
                                                   LocalDate start, LocalDate end) {
        // PostgreSQL CAST ... AS DATE — java.sql.Date 타입으로 받아서 DailySummaryDto에서 LocalDate 변환
        DateTemplate<java.sql.Date> measureDate = Expressions.dateTemplate(
                java.sql.Date.class, "CAST({0} AS DATE)", patientBiometricHistory.measuredAt);

        return queryFactory
                .select(Projections.constructor(DailySummaryDto.class,
                        patientDeviceAssignment.patient.id,
                        patientBiometricHistory.itemCode,
                        measureDate,
                        patientBiometricHistory.valueNumeric.avg(),
                        patientBiometricHistory.valueNumeric.min(),
                        patientBiometricHistory.valueNumeric.max(),
                        patientBiometricHistory.id.count()
                ))
                .from(patientBiometricHistory)
                .join(patientBiometricHistory.assignment, patientDeviceAssignment)
                .where(
                        patientIdsIn(patientIds),
                        itemCodesIn(itemCodes),
                        measuredAtGoe(start != null ? start.atStartOfDay() : null),
                        measuredAtLoe(end != null ? end.atTime(23, 59, 59) : null)
                )
                .groupBy(
                        patientDeviceAssignment.patient.id,
                        patientBiometricHistory.itemCode,
                        measureDate
                )
                .orderBy(
                        patientDeviceAssignment.patient.id.asc(),
                        measureDate.asc()
                )
                .fetch();
    }

    private BooleanExpression patientIdEq(Long patientId) {
        return patientId != null ? patientDeviceAssignment.patient.id.eq(patientId) : null;
    }

    private BooleanExpression patientIdsIn(List<Long> patientIds) {
        return !CollectionUtils.isEmpty(patientIds)
                ? patientDeviceAssignment.patient.id.in(patientIds)
                : null;
    }

    private BooleanExpression itemCodesIn(List<String> itemCodes) {
        return !CollectionUtils.isEmpty(itemCodes)
                ? patientBiometricHistory.itemCode.in(itemCodes)
                : null;
    }

    private BooleanExpression measuredAtGoe(LocalDateTime start) {
        return start != null ? patientBiometricHistory.measuredAt.goe(start) : null;
    }

    private BooleanExpression measuredAtLoe(LocalDateTime end) {
        return end != null ? patientBiometricHistory.measuredAt.loe(end) : null;
    }
}
