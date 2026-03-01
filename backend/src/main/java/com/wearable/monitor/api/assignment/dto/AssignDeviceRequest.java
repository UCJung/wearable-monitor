package com.wearable.monitor.api.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AssignDeviceRequest {

    @NotNull(message = "환자 ID를 입력해 주세요.")
    private Long patientId;

    @NotNull(message = "장치 ID를 입력해 주세요.")
    private Long deviceId;

    @NotNull(message = "모니터링 시작일을 입력해 주세요.")
    private LocalDate startDate;

    private LocalDate endDate;
}
