package com.wearable.monitor.api.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateDeviceRequest {

    @NotBlank(message = "시리얼 번호를 입력해 주세요.")
    @Size(max = 100, message = "시리얼 번호는 100자 이내로 입력해 주세요.")
    private String serialNumber;

    @Size(max = 100, message = "모델명은 100자 이내로 입력해 주세요.")
    private String modelName;
}
