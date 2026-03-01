package com.wearable.monitor.api.device.dto;

import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeviceListResponse {

    private final Long id;
    private final String serialNumber;
    private final String modelName;
    private final DeviceStatus deviceStatus;
    private final Integer batteryLevel;
    private final LocalDateTime lastSyncAt;
    private final String assignedPatientName;
    private final LocalDateTime createdAt;

    public DeviceListResponse(Device device, String assignedPatientName) {
        this.id = device.getId();
        this.serialNumber = device.getSerialNumber();
        this.modelName = device.getModelName();
        this.deviceStatus = device.getDeviceStatus();
        this.batteryLevel = device.getBatteryLevel();
        this.lastSyncAt = device.getLastSyncAt();
        this.assignedPatientName = assignedPatientName;
        this.createdAt = device.getCreatedAt();
    }
}
