package com.wearable.monitor.api.biometric;

import com.wearable.monitor.api.biometric.dto.BatchUploadRequest;
import com.wearable.monitor.api.biometric.dto.BatchUploadResponse;
import com.wearable.monitor.api.biometric.dto.BiometricHistoryResponse;
import com.wearable.monitor.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/biometric")
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricService biometricService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchUploadResponse>> batchUpload(
            @Valid @RequestBody BatchUploadRequest request) {
        BatchUploadResponse response = biometricService.batchUpload(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("배치 업로드가 완료되었습니다.", response));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<BiometricHistoryResponse>> getHistory(
            @PathVariable Long patientId,
            @RequestParam(required = false) List<String> itemCodes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 100) Pageable pageable) {
        BiometricHistoryResponse response = biometricService.getHistory(
                patientId, itemCodes, start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
