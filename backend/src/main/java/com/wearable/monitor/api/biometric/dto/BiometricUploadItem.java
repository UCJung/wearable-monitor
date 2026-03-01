package com.wearable.monitor.api.biometric.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BiometricUploadItem {

    @NotBlank(message = "항목 코드는 필수입니다.")
    private String itemCode;

    @NotNull(message = "측정 일시는 필수입니다.")
    private LocalDateTime measuredAt;

    private BigDecimal valueNumeric;

    private String valueText;

    private Integer durationSec;
}
