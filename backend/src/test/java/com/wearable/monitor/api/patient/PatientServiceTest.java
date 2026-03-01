package com.wearable.monitor.api.patient;

import com.wearable.monitor.api.patient.dto.*;
import com.wearable.monitor.common.ErrorCode;
import com.wearable.monitor.common.WearableException;
import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignmentRepository;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientRepository;
import com.wearable.monitor.domain.patient.PatientStatus;
import com.wearable.monitor.domain.user.User;
import com.wearable.monitor.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientDeviceAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PatientService patientService;

    @Test
    @DisplayName("createPatient_success — nextCode 호출 후 PT-0004 채번")
    void createPatient_success() {
        // given
        given(patientRepository.nextCode()).willReturn(4L);
        given(patientRepository.save(any(Patient.class))).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.existsByUsername("PT-0004")).willReturn(false);
        given(passwordEncoder.encode("PT-00041!")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        CreatePatientRequest request = mock(CreatePatientRequest.class);
        given(request.getName()).willReturn("테스트환자");
        given(request.getBirthDate()).willReturn(LocalDate.of(1990, 1, 1));
        given(request.getGender()).willReturn("M");
        given(request.getNotes()).willReturn(null);

        // when
        PatientDetailResponse response = patientService.createPatient(request);

        // then
        verify(patientRepository).nextCode();
        verify(patientRepository).save(any(Patient.class));
        assertThat(response.getPatientCode()).isEqualTo("PT-0004");
    }

    @Test
    @DisplayName("getPatientDetail_notFound — PATIENT_NOT_FOUND 예외")
    void getPatientDetail_notFound() {
        // given
        given(patientRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> patientService.getPatientDetail(999L))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PATIENT_NOT_FOUND));
    }

    @Test
    @DisplayName("updatePatient_success — update() 호출 확인")
    void updatePatient_success() {
        // given
        Patient patient = new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null);
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(any(), eq(AssignmentStatus.ACTIVE)))
                .willReturn(Optional.empty());
        given(assignmentRepository.findTop5ByPatientIdOrderByAssignedAtDesc(any()))
                .willReturn(List.of());

        UpdatePatientRequest request = mock(UpdatePatientRequest.class);
        given(request.getName()).willReturn("홍길동수정");
        given(request.getBirthDate()).willReturn(LocalDate.of(1980, 6, 15));
        given(request.getGender()).willReturn("M");
        given(request.getNotes()).willReturn("메모");

        // when
        PatientDetailResponse response = patientService.updatePatient(1L, request);

        // then
        assertThat(response.getName()).isEqualTo("홍길동수정");
    }

    @Test
    @DisplayName("deletePatient_hasActiveDevice — PATIENT_HAS_ACTIVE_DEVICE 예외")
    void deletePatient_hasActiveDevice() {
        // given
        Patient patient = new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null);
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.of(mock(PatientDeviceAssignment.class)));

        // when / then
        assertThatThrownBy(() -> patientService.deletePatient(1L))
                .isInstanceOf(WearableException.class)
                .satisfies(ex -> assertThat(((WearableException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PATIENT_HAS_ACTIVE_DEVICE));
    }

    @Test
    @DisplayName("deletePatient_success — changeStatus(DELETED) 호출")
    void deletePatient_success() {
        // given
        Patient patient = new Patient("PT-0001", "홍길동", LocalDate.of(1980, 1, 1), "M", null);
        given(patientRepository.findById(1L)).willReturn(Optional.of(patient));
        given(assignmentRepository.findByPatientIdAndAssignmentStatus(1L, AssignmentStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when
        patientService.deletePatient(1L);

        // then
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.DELETED);
    }
}
