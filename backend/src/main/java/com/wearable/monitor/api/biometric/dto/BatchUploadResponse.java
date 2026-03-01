package com.wearable.monitor.api.biometric.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchUploadResponse {

    private final int total;
    private final int saved;
    private final int skipped;
    private final int failed;
}
