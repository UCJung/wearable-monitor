package com.wearable.monitor.domain.biometric;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PatientBiometricHistoryRepositoryCustom {

    Page<PatientBiometricHistory> findByCondition(
            Long patientId,
            List<String> itemCodes,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<DailySummaryDto> findDailySummary(
            List<Long> patientIds,
            List<String> itemCodes,
            LocalDate start,
            LocalDate end);
}
