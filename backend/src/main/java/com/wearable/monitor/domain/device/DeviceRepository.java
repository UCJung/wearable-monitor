package com.wearable.monitor.domain.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    List<Device> findByDeviceStatus(DeviceStatus deviceStatus);
}
