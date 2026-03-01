package com.wearable.monitor.api.biometric.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchUploadRequest {

    @NotEmpty(message = "업로드 항목은 최소 1건 이상이어야 합니다.")
    @Valid
    private List<BiometricUploadItem> items;

    @NotBlank(message = "장치 시리얼 번호는 필수입니다.")
    private String deviceSerialNumber;
}
