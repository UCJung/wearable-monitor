package com.wearable.monitor.api.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreatePatientRequest {

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 이내로 입력해 주세요.")
    private String name;

    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @NotBlank(message = "성별을 입력해 주세요.")
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F로 입력해 주세요.")
    private String gender;

    private String notes;
}
