package com.wearable.monitor.api.monitoring;

import com.wearable.monitor.api.monitoring.dto.DailySummaryResponse;
import com.wearable.monitor.api.monitoring.dto.MonitoringSummaryResponse;
import com.wearable.monitor.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/assignment-status")
    public ResponseEntity<ApiResponse<MonitoringSummaryResponse>> getAssignmentStatus() {
        MonitoringSummaryResponse response = monitoringService.getAssignmentStatus();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<ApiResponse<DailySummaryResponse>> getDailySummary(
            @RequestParam(required = false) List<Long> patientIds,
            @RequestParam(required = false) List<String> itemCodes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        DailySummaryResponse response = monitoringService.getDailySummary(
                patientIds, itemCodes, start, end);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
