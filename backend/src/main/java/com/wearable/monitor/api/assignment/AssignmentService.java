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
import com.wearable.monitor.domain.patient.PatientStatus;
import com.wearable.monitor.domain.user.User;
import com.wearable.monitor.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final PatientDeviceAssignmentRepository assignmentRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentListResponse assignDevice(AssignDeviceRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> p.getStatus() != PatientStatus.DELETED)
                .orElseThrow(() -> new WearableException(ErrorCode.PATIENT_NOT_FOUND));

        Device device = deviceRepository.findById(request.getDeviceId())
                .filter(d -> d.getDeviceStatus() != DeviceStatus.RETIRED)
                .orElseThrow(() -> new WearableException(ErrorCode.DEVICE_NOT_FOUND));

        boolean patientHasDevice = assignmentRepository
                .findByPatientIdAndAssignmentStatus(patient.getId(), AssignmentStatus.ACTIVE)
                .isPresent();
        if (patientHasDevice) {
            throw new WearableException(ErrorCode.PATIENT_ALREADY_HAS_DEVICE);
        }

        if (device.getDeviceStatus() != DeviceStatus.AVAILABLE) {
            throw new WearableException(ErrorCode.DEVICE_ALREADY_ASSIGNED);
        }

        PatientDeviceAssignment assignment = new PatientDeviceAssignment(
                patient, device, request.getStartDate()
        );
        assignmentRepository.save(assignment);
        device.changeStatus(DeviceStatus.ASSIGNED);

        log.info("[AssignmentService] 장치 할당: patientId={}, deviceId={}", patient.getId(), device.getId());
        return new AssignmentListResponse(assignment);
    }

    @Transactional
    public AssignmentListResponse returnDevice(Long id, ReturnDeviceRequest request) {
        PatientDeviceAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new WearableException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        assignment.returnDevice(request.getEndDate());
        assignment.getDevice().changeStatus(DeviceStatus.AVAILABLE);

        log.info("[AssignmentService] 장치 반납: assignmentId={}", id);
        return new AssignmentListResponse(assignment);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentListResponse> getAssignments(Pageable pageable) {
        return assignmentRepository.findAll(pageable)
                .map(AssignmentListResponse::new);
    }

    @Transactional(readOnly = true)
    public AssignmentListResponse getAssignment(Long id) {
        PatientDeviceAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new WearableException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // PATIENT 사용자 본인 할당만 조회 가능
        validatePatientAccess(assignment.getPatient().getId());

        return new AssignmentListResponse(assignment);
    }

    private void validatePatientAccess(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;

        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        if (!isPatient) return;

        String username = (String) auth.getPrincipal();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new WearableException(ErrorCode.FORBIDDEN));

        if (user.getPatientId() == null || !user.getPatientId().equals(patientId)) {
            throw new WearableException(ErrorCode.FORBIDDEN);
        }
    }
}
