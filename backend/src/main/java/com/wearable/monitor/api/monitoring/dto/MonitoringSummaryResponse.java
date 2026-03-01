package com.wearable.monitor.api.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MonitoringSummaryResponse {

    private final SummaryCount summary;
    private final List<AssignmentStatusItem> patients;

    @Getter
    @AllArgsConstructor
    public static class SummaryCount {
        private final int total;
        private final int assigned;
        private final int collecting;
        private final int notCollecting;
    }
}
