package com.wearable.monitor.api.biometric;

import com.wearable.monitor.api.biometric.dto.*;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.PatientBiometricHistory;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinition;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinitionRepository;
import com.wearable.monitor.domain.user.User;
import com.wearable.monitor.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricService {

    private static final int MAX_BATCH_SIZE = 500;
    private static final int MAX_HISTORY_SIZE = 5000;
    private static final int MAX_HISTORY_DAYS = 90;

    private final PatientBiometricHistoryRepository biometricRepository;
    private final DeviceRepository deviceRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final CollectionItemDefinitionRepository itemDefRepository;
    private final UserRepository userRepository;

    @Transactional
    public BatchUploadResponse batchUpload(BatchUploadRequest request) {
        List<BiometricUploadItem> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new WearableException(ErrorCode.BATCH_EMPTY);
        }
        if (items.size() > MAX_BATCH_SIZE) {
            throw new WearableException(ErrorCode.BATCH_SIZE_EXCEEDED);
        }

        // 장치 조회
        Device device = deviceRepository.findBySerialNumber(request.getDeviceSerialNumber())
                .orElseThrow(() -> new WearableException(ErrorCode.DEVICE_NOT_FOUND));

        // 해당 장치의 활성 할당 조회
        PatientDeviceAssignment assignment = assignmentRepository
                .findByDeviceIdAndAssignmentStatus(device.getId(), AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new WearableException(ErrorCode.ASSIGNMENT_NOT_ACTIVE));

        // 수집 항목 정의 캐시 (배치 내 중복 조회 방지)
        Map<String, CollectionItemDefinition> itemDefMap = itemDefRepository
                .findByIsActiveTrueOrderByDisplayOrder().stream()
                .collect(Collectors.toMap(CollectionItemDefinition::getItemCode, d -> d));

        int saved = 0;
        int skipped = 0;
        int failed = 0;

        for (BiometricUploadItem item : items) {
            CollectionItemDefinition itemDef = itemDefMap.get(item.getItemCode());
            if (itemDef == null) {
                log.warn("Unknown item code: {}", item.getItemCode());
                failed++;
                continue;
            }

            // 중복 체크 (assignment_id + item_def_id + measured_at UNIQUE)
            if (biometricRepository.existsByAssignmentIdAndItemDef_IdAndMeasuredAt(
                    assignment.getId(), itemDef.getId(), item.getMeasuredAt())) {
                log.debug("Duplicate biometric record skipped: assignment={}, item={}, time={}",
                        assignment.getId(), item.getItemCode(), item.getMeasuredAt());
                skipped++;
                continue;
            }

            PatientBiometricHistory history = new PatientBiometricHistory(
                    assignment, itemDef, item.getMeasuredAt(),
                    item.getValueNumeric(), item.getValueText(), null,
                    item.getDurationSec()
            );
            biometricRepository.save(history);
            saved++;
        }

        // 장치 last_sync_at 갱신
        device.updateLastSyncAt(LocalDateTime.now());

        log.info("Batch upload completed: total={}, saved={}, skipped={}, failed={}",
                items.size(), saved, skipped, failed);

        return new BatchUploadResponse(items.size(), saved, skipped, failed);
    }

    @Transactional(readOnly = true)
    public BiometricHistoryResponse getHistory(Long patientId, List<String> itemCodes,
                                                LocalDate start, LocalDate end,
                                                Pageable pageable) {
        // PATIENT 사용자 본인 데이터만 조회 가능
        validatePatientAccess(patientId);

        // 최대 90일 범위 검증
        if (start != null && end != null) {
            long days = ChronoUnit.DAYS.between(start, end);
            if (days > MAX_HISTORY_DAYS) {
                throw new WearableException(ErrorCode.DATE_RANGE_EXCEEDED,
                        "최대 " + MAX_HISTORY_DAYS + "일까지 조회 가능합니다.");
            }
        }

        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;

        // 최대 5,000건 반환
        Pageable limitedPageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_HISTORY_SIZE),
                pageable.getSort()
        );

        Page<PatientBiometricHistory> page = biometricRepository.findByCondition(
                patientId, itemCodes, startDateTime, endDateTime, limitedPageable);

        List<BiometricHistoryResponse.BiometricRecord> records = page.getContent().stream()
                .map(h -> new BiometricHistoryResponse.BiometricRecord(
                        h.getId(),
                        h.getItemCode(),
                        h.getItemDef().getItemNameKo(),
                        categoryToKorean(h.getItemDef().getCategory().name()),
                        h.getMeasuredAt(),
                        h.getValueNumeric(),
                        h.getValueText(),
                        h.getItemDef().getUnit(),
                        h.getDurationSec()
                ))
                .collect(Collectors.toList());

        boolean hasMore = page.getTotalElements() > MAX_HISTORY_SIZE;

        return new BiometricHistoryResponse(records, page.getTotalElements(), hasMore);
    }

    private void validatePatientAccess(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;

        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        if (!isPatient) return;

        String username = (String) auth.getPrincipal();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new WearableException(ErrorCode.FORBIDDEN));

        if (user.getPatientId() == null || !user.getPatientId().equals(patientId)) {
            throw new WearableException(ErrorCode.FORBIDDEN);
        }
    }

    private String categoryToKorean(String category) {
        return switch (category) {
            case "VITAL_SIGN" -> "생체신호";
            case "ACTIVITY" -> "활동";
            case "SLEEP" -> "수면";
            case "AI_SCORE" -> "AI종합";
            default -> category;
        };
    }
}
