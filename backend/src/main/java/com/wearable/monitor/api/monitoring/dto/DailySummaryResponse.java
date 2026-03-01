package com.wearable.monitor.api.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class DailySummaryResponse {

    private final List<DailySummaryItem> items;

    @Getter
    @AllArgsConstructor
    public static class DailySummaryItem {
        private final Long patientId;
        private final String itemCode;
        private final LocalDate measureDate;
        private final Double avgValue;
        private final BigDecimal minValue;
        private final BigDecimal maxValue;
        private final Long measureCount;
    }
}
