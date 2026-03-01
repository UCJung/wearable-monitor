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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public PatientDetailResponse createPatient(CreatePatientRequest request) {
        Long nextCode = patientRepository.nextCode();
        String patientCode = String.format("PT-%04d", nextCode);

        Patient patient = new Patient(
                patientCode,
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                request.getNotes()
        );
        patientRepository.save(patient);
        entityManager.flush();

        // 환자 계정 자동 생성 (초기 비밀번호: 환자코드 + "1!")
        if (!userRepository.existsByUsername(patientCode)) {
            String encodedPassword = passwordEncoder.encode(patientCode + "1!");
            User patientUser = User.forPatient(patientCode, encodedPassword, patient.getId());
            userRepository.save(patientUser);
            log.info("[PatientService] 환자 계정 자동 생성: username={}", patientCode);
        }

        log.info("[PatientService] 환자 생성: code={}", patientCode);
        return new PatientDetailResponse(patient, null, List.of());
    }

    @Transactional(readOnly = true)
    public Page<PatientListResponse> getPatients(PatientSearchCondition condition, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByCondition(condition, pageable);
        return patients.map(p -> {
            Optional<PatientDeviceAssignment> activeAssignment =
                    assignmentRepository.findByPatientIdAndAssignmentStatus(p.getId(), AssignmentStatus.ACTIVE);
            return new PatientListResponse(p, activeAssignment.isPresent());
        });
    }

    @Transactional(readOnly = true)
    public PatientDetailResponse getPatientDetail(Long id) {
        Patient patient = findPatientById(id);

        Optional<PatientDeviceAssignment> activeAssignment =
                assignmentRepository.findByPatientIdAndAssignmentStatus(id, AssignmentStatus.ACTIVE);

        List<PatientDeviceAssignment> history =
                assignmentRepository.findTop5ByPatientIdOrderByAssignedAtDesc(id);

        return new PatientDetailResponse(patient, activeAssignment.orElse(null), history);
    }

    @Transactional
    public PatientDetailResponse updatePatient(Long id, UpdatePatientRequest request) {
        Patient patient = findPatientById(id);
        patient.update(request.getName(), request.getBirthDate(), request.getGender(), request.getNotes());

        log.info("[PatientService] 환자 수정: id={}", id);
        return getPatientDetail(id);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = findPatientById(id);

        boolean hasActiveDevice = assignmentRepository
                .findByPatientIdAndAssignmentStatus(id, AssignmentStatus.ACTIVE)
                .isPresent();

        if (hasActiveDevice) {
            throw new WearableException(ErrorCode.PATIENT_HAS_ACTIVE_DEVICE);
        }

        patient.changeStatus(PatientStatus.DELETED);
        log.info("[PatientService] 환자 삭제(소프트): id={}", id);
    }

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .filter(p -> p.getStatus() != PatientStatus.DELETED)
                .orElseThrow(() -> new WearableException(ErrorCode.PATIENT_NOT_FOUND));
    }
}
