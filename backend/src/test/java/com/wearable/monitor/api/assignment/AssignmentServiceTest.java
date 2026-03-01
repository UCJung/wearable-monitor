package com.wearable.monitor.api.assignment;

import com.wearable.monitor.api.assignment.dto.AssignDeviceRequest;
import com.wearable.monitor.api.assignment.dto.AssignmentListResponse;
import com.wearable.monitor.api.assignment.dto.ReturnDeviceRequest;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceRepository;
import com.wearable.monitor.domain.device.DeviceStatus;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Patient buildPatient(Long id) {
        Patient patient = new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null);
        // 리플렉션으로 id 설정 불가하므로 spy 사용
        Patient spy = spy(patient);
        lenient().when(spy.getId()).thenReturn(id);
        return spy;
    }

    private Device buildDevice(Long id, DeviceStatus status) {
        Device device = new Device("SN-001", "Galaxy Watch 7");
        if (status != DeviceStatus.AVAILABLE) {
            device.changeStatus(status);
        }
        Device spy = spy(device);
        lenient().when(spy.getId()).thenReturn(id);
        return spy;
    }

    @Test
    @DisplayName("assignDevice_patientAlreadyHasDevice — PATIENT_ALREADY_HAS_DEVICE 예외")
    void assignDevice_patientAlreadyHasDevice() {
        // given
        Patient patient = buildPatient(1L);
        Device device = buildDevice(2L, DeviceStatus.AVAILABLE);

        AssignDeviceRequest request = mock(AssignDeviceRequest.class);
        given(request.getPatientId()).willReturn(1L);
        given(request.getDeviceId()).willReturn(2L);

        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(deviceRepository.findById(2L)).willReturn(Optional.of(device));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(mock(PatientDeviceAssignment.class)));

        // when / then
        assertThatThrownBy(() -> assignmentService.assignDevice(request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PATIENT_ALREADY_HAS_DEVICE));
    }

    @Test
    @DisplayName("assignDevice_deviceAlreadyAssigned — DEVICE_ALREADY_ASSIGNED 예외")
    void assignDevice_deviceAlreadyAssigned() {
        // given
        Patient patient = buildPatient(1L);
        Device device = buildDevice(2L, DeviceStatus.ASSIGNED);

        AssignDeviceRequest request = mock(AssignDeviceRequest.class);
        given(request.getPatientId()).willReturn(1L);
        given(request.getDeviceId()).willReturn(2L);

        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(deviceRepository.findById(2L)).willReturn(Optional.of(device));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> assignmentService.assignDevice(request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DEVICE_ALREADY_ASSIGNED));
    }

    @Test
    @DisplayName("assignDevice_success — Assignment 저장, device ASSIGNED 변경")
    void assignDevice_success() {
        // given
        Patient patient = buildPatient(1L);
        Device device = buildDevice(2L, DeviceStatus.AVAILABLE);

        AssignDeviceRequest request = mock(AssignDeviceRequest.class);
        given(request.getPatientId()).willReturn(1L);
        given(request.getDeviceId()).willReturn(2L);
        given(request.getStartDate()).willReturn(LocalDate.of(2026, 3, 1));

        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(deviceRepository.findById(2L)).willReturn(Optional.of(device));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(assignmentRepository.save(any(PatientDeviceAssignment.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        AssignmentListResponse response = assignmentService.assignDevice(request);

        // then
        verify(assignmentRepository).save(any(PatientDeviceAssignment.class));
        assertThat(device.getDeviceStatus()).isEqualTo(DeviceStatus.ASSIGNED);
    }

    @Test
    @DisplayName("returnDevice_notFound — ASSIGNMENT_NOT_FOUND 예외")
    void returnDevice_notFound() {
        // given
        given(assignmentRepository.findById(999L)).willReturn(Optional.empty());
        ReturnDeviceRequest request = mock(ReturnDeviceRequest.class);

        // when / then
        assertThatThrownBy(() -> assignmentService.returnDevice(999L, request))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ASSIGNMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("returnDevice_success — returnDevice() 호출, device AVAILABLE 변경")
    void returnDevice_success() {
        // given
        Patient patient = buildPatient(1L);
        Device device = buildDevice(2L, DeviceStatus.ASSIGNED);
        PatientDeviceAssignment assignment = new PatientDeviceAssignment(
                patient, device, LocalDate.of(2026, 1, 1));

        ReturnDeviceRequest request = mock(ReturnDeviceRequest.class);
        given(request.getEndDate()).willReturn(LocalDate.of(2026, 3, 1));

        given(assignmentRepository.findById(1L)).willReturn(Optional.of(assignment));

        // when
        assignmentService.returnDevice(1L, request);

        // then
        assertThat(assignment.getAssignmentStatus()).isEqualTo(AssignmentStatus.RETURNED);
        assertThat(device.getDeviceStatus()).isEqualTo(DeviceStatus.AVAILABLE);
    }
}
