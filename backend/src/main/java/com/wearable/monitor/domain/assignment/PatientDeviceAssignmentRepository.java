package com.wearable.monitor.domain.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientDeviceAssignmentRepository extends JpaRepository<PatientDeviceAssignment, Long> {

    Optional<PatientDeviceAssignment> findByPatientIdAndAssignmentStatus(
            Long patientId, AssignmentStatus assignmentStatus);

    Optional<PatientDeviceAssignment> findByDeviceIdAndAssignmentStatus(
            Long deviceId, AssignmentStatus assignmentStatus);

    List<PatientDeviceAssignment> findTop5ByPatientIdOrderByAssignedAtDesc(Long patientId);

    List<PatientDeviceAssignment> findTop5ByDeviceIdOrderByAssignedAtDesc(Long deviceId);
}
