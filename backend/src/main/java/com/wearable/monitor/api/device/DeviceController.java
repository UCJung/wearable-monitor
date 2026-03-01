package com.wearable.monitor.api.device;

import com.wearable.monitor.api.device.dto.*;
import com.wearable.monitor.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceDetailResponse>> createDevice(
            @Valid @RequestBody CreateDeviceRequest request) {
        DeviceDetailResponse response = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeviceListResponse>>> getDevices(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DeviceListResponse> response = deviceService.getDevices(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceDetailResponse>> getDeviceDetail(@PathVariable Long id) {
        DeviceDetailResponse response = deviceService.getDeviceDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceDetailResponse>> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request) {
        DeviceDetailResponse response = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
