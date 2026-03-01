package com.wearable.monitor.domain.biometric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientBiometricHistoryRepository
        extends JpaRepository<PatientBiometricHistory, Long>,
                PatientBiometricHistoryRepositoryCustom {

    @Query("SELECT MAX(h.measuredAt) FROM PatientBiometricHistory h WHERE h.assignment.id = :assignmentId")
    Optional<LocalDateTime> findLatestMeasuredAt(@Param("assignmentId") Long assignmentId);

    List<PatientBiometricHistory> findByAssignment_Patient_IdOrderByMeasuredAtDesc(Long patientId);

    boolean existsByAssignmentIdAndItemDef_IdAndMeasuredAt(Long assignmentId, Long itemDefId, LocalDateTime measuredAt);
}
