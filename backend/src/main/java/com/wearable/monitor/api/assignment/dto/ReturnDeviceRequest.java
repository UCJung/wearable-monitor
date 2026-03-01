package com.wearable.monitor.api.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ReturnDeviceRequest {

    @NotNull(message = "모니터링 종료일을 입력해 주세요.")
    private LocalDate endDate;
}
