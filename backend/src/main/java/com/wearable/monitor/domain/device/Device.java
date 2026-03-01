package com.wearable.monitor.domain.device;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", unique = true, nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 20)
    private DeviceStatus deviceStatus = DeviceStatus.AVAILABLE;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Device(String serialNumber, String modelName) {
        this.serialNumber = serialNumber;
        this.modelName = modelName;
        this.deviceStatus = DeviceStatus.AVAILABLE;
    }

    public void changeStatus(DeviceStatus status) {
        this.deviceStatus = status;
    }

    public void updateBatteryAndSync(Integer batteryLevel, LocalDateTime lastSyncAt) {
        this.batteryLevel = batteryLevel;
        this.lastSyncAt = lastSyncAt;
    }

    public void updateLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
}
