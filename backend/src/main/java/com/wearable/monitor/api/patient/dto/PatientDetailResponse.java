package com.wearable.monitor.api.patient.dto;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PatientDetailResponse {

    private final Long id;
    private final String patientCode;
    private final String name;
    private final LocalDate birthDate;
    private final String gender;
    private final String notes;
    private final PatientStatus status;
    private final LocalDateTime createdAt;
    private final CurrentDevice currentDevice;
    private final List<AssignmentHistory> assignmentHistory;

    public PatientDetailResponse(Patient patient,
                                  PatientDeviceAssignment currentAssignment,
                                  List<PatientDeviceAssignment> history) {
        this.id = patient.getId();
        this.patientCode = patient.getPatientCode();
        this.name = patient.getName();
        this.birthDate = patient.getBirthDate();
        this.gender = patient.getGender();
        this.notes = patient.getNotes();
        this.status = patient.getStatus();
        this.createdAt = patient.getCreatedAt();
        this.currentDevice = currentAssignment != null ? new CurrentDevice(currentAssignment) : null;
        this.assignmentHistory = history.stream().map(AssignmentHistory::new).toList();
    }

    @Getter
    public static class CurrentDevice {
        private final Long assignmentId;
        private final Long deviceId;
        private final String serialNumber;
        private final String modelName;
        private final LocalDate monitoringStartDate;
        private final AssignmentStatus assignmentStatus;

        public CurrentDevice(PatientDeviceAssignment assignment) {
            this.assignmentId = assignment.getId();
            this.deviceId = assignment.getDevice().getId();
            this.serialNumber = assignment.getDevice().getSerialNumber();
            this.modelName = assignment.getDevice().getModelName();
            this.monitoringStartDate = assignment.getMonitoringStartDate();
            this.assignmentStatus = assignment.getAssignmentStatus();
        }
    }

    @Getter
    public static class AssignmentHistory {
        private final Long assignmentId;
        private final String serialNumber;
        private final LocalDate monitoringStartDate;
        private final LocalDate monitoringEndDate;
        private final AssignmentStatus assignmentStatus;
        private final LocalDateTime assignedAt;
        private final LocalDateTime returnedAt;

        public AssignmentHistory(PatientDeviceAssignment assignment) {
            this.assignmentId = assignment.getId();
            this.serialNumber = assignment.getDevice().getSerialNumber();
            this.monitoringStartDate = assignment.getMonitoringStartDate();
            this.monitoringEndDate = assignment.getMonitoringEndDate();
            this.assignmentStatus = assignment.getAssignmentStatus();
            this.assignedAt = assignment.getAssignedAt();
            this.returnedAt = assignment.getReturnedAt();
        }
    }
}
