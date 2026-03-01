package com.wearable.monitor.api.monitoring;

import com.wearable.monitor.api.monitoring.dto.DailySummaryResponse;
import com.wearable.monitor.api.monitoring.dto.MonitoringSummaryResponse;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.biometric.DailySummaryDto;
import com.wearable.monitor.domain.biometric.PatientBiometricHistoryRepository;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.patient.PatientStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;
    @Mock
    private PatientBiometricHistoryRepository biometricRepository;

    @InjectMocks
    private MonitoringService monitoringService;

    private Patient buildPatient(Long id, String code, String name) {
        Patient patient = spy(new Patient(code, name, LocalDate.of(1980, 1, 1), "M", null));
        lenient().when(patient.getId()).thenReturn(id);
        return patient;
    }

    private Device buildDevice(Long id) {
        Device device = spy(new Device("SN-WATCH-001", "Galaxy Watch 7"));
        lenient().when(device.getId()).thenReturn(id);
        return device;
    }

    @Test
    @DisplayName("getAssignmentStatus — 24시간 이내 수집 환자 → recentlyCollected true")
    void getAssignmentStatus_recentlyCollected() {
        // given
        Patient patient = buildPatient(1L, "PT-0001", "홍길동");
        Device device = buildDevice(10L);
        PatientDeviceAssignment assignment = spy(new PatientDeviceAssignment(
                patient, device, LocalDate.of(2026, 1, 1)));
        lenient().when(assignment.getId()).thenReturn(100L);

        given(patientRepository.findByStatusNot(PatientStatus.DELETED))
                .willReturn(List.of(patient));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(biometricRepository.findLatestMeasuredAt(100L))
                .willReturn(Optional.of(LocalDateTime.now().minusHours(1))); // 1시간 전

        // when
        MonitoringSummaryResponse response = monitoringService.getAssignmentStatus();

        // then
        assertThat(response.getPatients()).hasSize(1);
        assertThat(response.getPatients().get(0).isRecentlyCollected()).isTrue();
        assertThat(response.getSummary().getCollecting()).isEqualTo(1);
        assertThat(response.getSummary().getNotCollecting()).isEqualTo(0);
    }

    @Test
    @DisplayName("getAssignmentStatus — 24시간 초과 수집 환자 → recentlyCollected false")
    void getAssignmentStatus_notRecentlyCollected() {
        // given
        Patient patient = buildPatient(1L, "PT-0001", "홍길동");
        Device device = buildDevice(10L);
        PatientDeviceAssignment assignment = spy(new PatientDeviceAssignment(
                patient, device, LocalDate.of(2026, 1, 1)));
        lenient().when(assignment.getId()).thenReturn(100L);

        given(patientRepository.findByStatusNot(PatientStatus.DELETED))
                .willReturn(List.of(patient));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(biometricRepository.findLatestMeasuredAt(100L))
                .willReturn(Optional.of(LocalDateTime.now().minusHours(25))); // 25시간 전

        // when
        MonitoringSummaryResponse response = monitoringService.getAssignmentStatus();

        // then
        assertThat(response.getPatients().get(0).isRecentlyCollected()).isFalse();
        assertThat(response.getSummary().getCollecting()).isEqualTo(0);
        assertThat(response.getSummary().getNotCollecting()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAssignmentStatus — 미할당 환자 포함, 요약 카운트 정확")
    void getAssignmentStatus_summaryCount() {
        // given
        Patient p1 = buildPatient(1L, "PT-0001", "홍길동");
        Patient p2 = buildPatient(2L, "PT-0002", "김영희");

        Device device = buildDevice(10L);
        PatientDeviceAssignment assignment = spy(new PatientDeviceAssignment(
                p1, device, LocalDate.of(2026, 1, 1)));
        lenient().when(assignment.getId()).thenReturn(100L);

        given(patientRepository.findByStatusNot(PatientStatus.DELETED))
                .willReturn(List.of(p1, p2));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(assignment));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(2L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.empty()); // 미할당
        given(biometricRepository.findLatestMeasuredAt(100L))
                .willReturn(Optional.of(LocalDateTime.now().minusHours(1)));

        // when
        MonitoringSummaryResponse response = monitoringService.getAssignmentStatus();

        // then
        assertThat(response.getSummary().getTotal()).isEqualTo(2);
        assertThat(response.getSummary().getAssigned()).isEqualTo(1);
        assertThat(response.getSummary().getCollecting()).isEqualTo(1);
    }

    @Test
    @DisplayName("getDailySummary — 31일 초과 시 DATE_RANGE_EXCEEDED 예외")
    void getDailySummary_exceedsMaxDays() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 2, 2); // 32일

        assertThatThrownBy(() -> monitoringService.getDailySummary(null, null, start, end))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DATE_RANGE_EXCEEDED));
    }

    @Test
    @DisplayName("getDailySummary — 정상 조회 시 결과 반환")
    void getDailySummary_success() {
        // given
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);

        DailySummaryDto dto = new DailySummaryDto(1L, "HR", LocalDate.of(2026, 2, 1),
                72.5, BigDecimal.valueOf(60), BigDecimal.valueOf(85), 24L);

        given(biometricRepository.findDailySummary(any(), any(), eq(start), eq(end)))
                .willReturn(List.of(dto));

        // when
        DailySummaryResponse response = monitoringService.getDailySummary(null, null, start, end);

        // then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemCode()).isEqualTo("HR");
        assertThat(response.getItems().get(0).getAvgValue()).isEqualTo(72.5);
    }
}
