package com.wearable.monitor.api.biometric.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BiometricHistoryResponse {

    private final List<BiometricRecord> records;
    private final long totalCount;
    private final boolean hasMore;

    @Getter
    @AllArgsConstructor
    public static class BiometricRecord {
        private final Long id;
        private final String itemCode;
        private final String itemNameKo;
        private final String category;
        private final LocalDateTime measuredAt;
        private final BigDecimal valueNumeric;
        private final String valueText;
        private final String unit;
        private final Integer durationSec;
    }
}
