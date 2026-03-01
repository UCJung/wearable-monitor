package com.wearable.monitor.api.device;

import com.wearable.monitor.api.device.dto.*;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.device.DeviceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;

    @Transactional
    public DeviceDetailResponse createDevice(CreateDeviceRequest request) {
        if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new WearableException(ErrorCode.DEVICE_SERIAL_DUPLICATE);
        }

        Device device = new Device(request.getSerialNumber(), request.getModelName());
        deviceRepository.save(device);

        log.info("[DeviceService] 장치 생성: serial={}", request.getSerialNumber());
        return new DeviceDetailResponse(device, null, List.of());
    }

    @Transactional(readOnly = true)
    public Page<DeviceListResponse> getDevices(Pageable pageable) {
        Page<Device> devices = deviceRepository.findAll(pageable);
        return devices.map(device -> {
            String assignedPatientName = null;
            if (device.getDeviceStatus() == DeviceStatus.ASSIGNED) {
                Optional<PatientDeviceAssignment> activeAssignment =
                        assignmentRepository.findByDeviceIdAndAssignmentStatus(device.getId(), AssignmentStatus.ACTIVE);
                assignedPatientName = activeAssignment
                        .map(a -> a.getPatient().getName())
                        .orElse(null);
            }
            return new DeviceListResponse(device, assignedPatientName);
        });
    }

    @Transactional(readOnly = true)
    public DeviceDetailResponse getDeviceDetail(Long id) {
        Device device = findDeviceById(id);

        Optional<PatientDeviceAssignment> activeAssignment =
                assignmentRepository.findByDeviceIdAndAssignmentStatus(id, AssignmentStatus.ACTIVE);

        List<PatientDeviceAssignment> history =
                assignmentRepository.findTop5ByDeviceIdOrderByAssignedAtDesc(id);

        return new DeviceDetailResponse(device, activeAssignment.orElse(null), history);
    }

    @Transactional
    public DeviceDetailResponse updateDevice(Long id, UpdateDeviceRequest request) {
        Device device = findDeviceById(id);

        if (request.getModelName() != null || request.getBatteryLevel() != null) {
            // modelName 업데이트는 별도 메서드가 없으므로 배터리/동기화 메서드 활용
            // Device 엔티티에 update 메서드가 없어 직접 필드 업데이트
            device.updateBatteryAndSync(
                    request.getBatteryLevel() != null ? request.getBatteryLevel() : device.getBatteryLevel(),
                    device.getLastSyncAt()
            );
        }

        log.info("[DeviceService] 장치 수정: id={}", id);
        return getDeviceDetail(id);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = findDeviceById(id);

        if (device.getDeviceStatus() == DeviceStatus.ASSIGNED) {
            throw new WearableException(ErrorCode.DEVICE_IS_ASSIGNED_CANNOT_DELETE);
        }

        device.changeStatus(DeviceStatus.RETIRED);
        log.info("[DeviceService] 장치 삭제(소프트): id={}", id);
    }

    private Device findDeviceById(Long id) {
        return deviceRepository.findById(id)
                .filter(d -> d.getDeviceStatus() != DeviceStatus.RETIRED)
                .orElseThrow(() -> new WearableException(ErrorCode.DEVICE_NOT_FOUND));
    }
}
