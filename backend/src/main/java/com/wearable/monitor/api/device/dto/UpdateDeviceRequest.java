package com.wearable.monitor.api.device.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateDeviceRequest {

    @Size(max = 100, message = "모델명은 100자 이내로 입력해 주세요.")
    private String modelName;

    @Min(value = 0, message = "배터리 레벨은 0 이상이어야 합니다.")
    @Max(value = 100, message = "배터리 레벨은 100 이하이어야 합니다.")
    private Integer batteryLevel;
}
