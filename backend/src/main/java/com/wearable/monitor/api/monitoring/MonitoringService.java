package com.wearable.monitor.api.monitoring;

import com.wearable.monitor.api.monitoring.dto.*;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.DailySummaryDto;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.patient.PatientStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private static final int MAX_DAILY_SUMMARY_DAYS = 31;

    private final PatientRepository patientRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final PatientBiometricHistoryRepository biometricRepository;

    @Transactional(readOnly = true)
    public MonitoringSummaryResponse getAssignmentStatus() {
        List<Patient> patients = patientRepository.findByStatusNot(PatientStatus.DELETED);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold24h = now.minusHours(24);

        int totalCount = patients.size();
        int assignedCount = 0;
        int collectingCount = 0;
        int notCollectingCount = 0;

        List<AssignmentStatusItem> items = new ArrayList<>();

        for (Patient patient : patients) {
            Optional<PatientDeviceAssignment> activeAssignment = assignmentRepository
                    .findByPatientIdAndAssignmentStatus(patient.getId(), AssignmentStatus.ACTIVE);

            LocalDateTime lastCollectedAt = null;
            boolean recentlyCollected = false;
            Long deviceId = null;
            String serialNumber = null;
            String modelName = null;
            Integer batteryLevel = null;

            if (activeAssignment.isPresent()) {
                assignedCount++;
                PatientDeviceAssignment assignment = activeAssignment.get();
                deviceId = assignment.getDevice().getId();
                serialNumber = assignment.getDevice().getSerialNumber();
                modelName = assignment.getDevice().getModelName();
                batteryLevel = assignment.getDevice().getBatteryLevel();

                Optional<LocalDateTime> latestMeasured = biometricRepository
                        .findLatestMeasuredAt(assignment.getId());
                if (latestMeasured.isPresent()) {
                    lastCollectedAt = latestMeasured.get();
                    recentlyCollected = lastCollectedAt.isAfter(threshold24h);
                    if (recentlyCollected) {
                        collectingCount++;
                    } else {
                        notCollectingCount++;
                    }
                } else {
                    notCollectingCount++;
                }
            }

            items.add(AssignmentStatusItem.builder()
                    .patientId(patient.getId())
                    .patientCode(patient.getPatientCode())
                    .patientName(patient.getName())
                    .status(patient.getStatus().name())
                    .deviceId(deviceId)
                    .serialNumber(serialNumber)
                    .modelName(modelName)
                    .batteryLevel(batteryLevel)
                    .lastCollectedAt(lastCollectedAt)
                    .recentlyCollected(recentlyCollected)
                    .build());
        }

        MonitoringSummaryResponse.SummaryCount summary =
                new MonitoringSummaryResponse.SummaryCount(
                        totalCount, assignedCount, collectingCount, notCollectingCount);

        return new MonitoringSummaryResponse(summary, items);
    }

    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(List<Long> patientIds, List<String> itemCodes,
                                                 LocalDate start, LocalDate end) {
        // 최대 31일 범위 검증
        if (start != null && end != null) {
            long days = ChronoUnit.DAYS.between(start, end);
            if (days > MAX_DAILY_SUMMARY_DAYS) {
                throw new WearableException(ErrorCode.DATE_RANGE_EXCEEDED,
                        "일별 요약은 최대 " + MAX_DAILY_SUMMARY_DAYS + "일까지 조회 가능합니다.");
            }
        }

        List<DailySummaryDto> summaries = biometricRepository.findDailySummary(
                patientIds, itemCodes, start, end);

        List<DailySummaryResponse.DailySummaryItem> items = summaries.stream()
                .map(s -> new DailySummaryResponse.DailySummaryItem(
                        s.getPatientId(),
                        s.getItemCode(),
                        s.getMeasureDate(),
                        s.getAvgValue(),
                        s.getMinValue(),
                        s.getMaxValue(),
                        s.getMeasureCount()
                ))
                .collect(Collectors.toList());

        return new DailySummaryResponse(items);
    }
}
