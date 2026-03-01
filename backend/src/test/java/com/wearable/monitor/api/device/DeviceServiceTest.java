package com.wearable.monitor.api.device;

import com.wearable.monitor.api.device.dto.*;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.device.DeviceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    @DisplayName("createDevice_duplicateSerial — DEVICE_SERIAL_DUPLICATE 예외")
    void createDevice_duplicateSerial() {
        // given
        CreateDeviceRequest request = mock(CreateDeviceRequest.class);
        given(request.getSerialNumber()).willReturn("SN-001");
        given(deviceRepository.existsBySerialNumber("SN-001")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> deviceService.createDevice(request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DEVICE_SERIAL_DUPLICATE));
    }

    @Test
    @DisplayName("createDevice_success — save 호출")
    void createDevice_success() {
        // given
        CreateDeviceRequest request = mock(CreateDeviceRequest.class);
        given(request.getSerialNumber()).willReturn("SN-NEW-001");
        given(request.getModelName()).willReturn("Galaxy Watch 7");
        given(deviceRepository.existsBySerialNumber("SN-NEW-001")).willReturn(false);
        given(deviceRepository.save(any(Device.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        DeviceDetailResponse response = deviceService.createDevice(request);

        // then
        verify(deviceRepository).save(any(Device.class));
        assertThat(response.getSerialNumber()).isEqualTo("SN-NEW-001");
    }

    @Test
    @DisplayName("deleteDevice_assigned — DEVICE_IS_ASSIGNED_CANNOT_DELETE 예외")
    void deleteDevice_assigned() {
        // given
        Device device = new Device("SN-001", "Galaxy Watch 7");
        device.changeStatus(DeviceStatus.ASSIGNED);
        given(deviceRepository.findById(1L)).willReturn(Optional.of(device));

        // when / then
        assertThatThrownBy(() -> deviceService.deleteDevice(1L))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DEVICE_IS_ASSIGNED_CANNOT_DELETE));
    }

    @Test
    @DisplayName("deleteDevice_success — changeStatus(RETIRED) 호출")
    void deleteDevice_success() {
        // given
        Device device = new Device("SN-001", "Galaxy Watch 7");
        given(deviceRepository.findById(1L)).willReturn(Optional.of(device));

        // when
        deviceService.deleteDevice(1L);

        // then
        assertThat(device.getDeviceStatus()).isEqualTo(DeviceStatus.RETIRED);
    }
}
