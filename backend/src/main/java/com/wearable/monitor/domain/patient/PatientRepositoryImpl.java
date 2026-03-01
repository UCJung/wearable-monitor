package com.wearable.monitor.domain.patient;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wearable.monitor.api.patient.dto.PatientSearchCondition;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.QPatientDeviceAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QPatient patient = QPatient.patient;
    private final QPatientDeviceAssignment assignment = QPatientDeviceAssignment.patientDeviceAssignment;

    @Override
    public Page<Patient> findByCondition(PatientSearchCondition condition, Pageable pageable) {
        List<Patient> content = queryFactory
                .selectFrom(patient)
                .where(
                        notDeleted(),
                        nameLike(condition.getName()),
                        patientCodeLike(condition.getPatientCode()),
                        statusEq(condition.getStatus()),
                        hasDevice(condition.getHasDevice())
                )
                .orderBy(patient.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(patient.count())
                .from(patient)
                .where(
                        notDeleted(),
                        nameLike(condition.getName()),
                        patientCodeLike(condition.getPatientCode()),
                        statusEq(condition.getStatus()),
                        hasDevice(condition.getHasDevice())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression notDeleted() {
        return patient.status.ne(PatientStatus.DELETED);
    }

    private BooleanExpression nameLike(String name) {
        return StringUtils.hasText(name) ? patient.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression patientCodeLike(String patientCode) {
        return StringUtils.hasText(patientCode) ? patient.patientCode.containsIgnoreCase(patientCode) : null;
    }

    private BooleanExpression statusEq(PatientStatus status) {
        return status != null ? patient.status.eq(status) : null;
    }

    private BooleanExpression hasDevice(Boolean hasDevice) {
        if (hasDevice == null) {
            return null;
        }
        BooleanExpression existsActive = JPAExpressions
                .selectOne()
                .from(assignment)
                .where(
                        assignment.patient.id.eq(patient.id),
                        assignment.assignmentStatus.eq(AssignmentStatus.ACTIVE)
                )
                .exists();
        return hasDevice ? existsActive : existsActive.not();
    }
}
