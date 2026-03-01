package com.wearable.monitor.api.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AssignmentStatusItem {

    private final Long patientId;
    private final String patientCode;
    private final String patientName;
    private final String status;

    // 할당 장치 정보 (없으면 null)
    private final Long deviceId;
    private final String serialNumber;
    private final String modelName;
    private final Integer batteryLevel;

    // 수집 상태
    private final LocalDateTime lastCollectedAt;
    private final boolean recentlyCollected;
}
