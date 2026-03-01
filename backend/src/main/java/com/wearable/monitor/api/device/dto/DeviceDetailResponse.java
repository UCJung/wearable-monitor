package com.wearable.monitor.api.device.dto;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.device.DeviceStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DeviceDetailResponse {

    private final Long id;
    private final String serialNumber;
    private final String modelName;
    private final DeviceStatus deviceStatus;
    private final Integer batteryLevel;
    private final LocalDateTime lastSyncAt;
    private final LocalDateTime createdAt;
    private final CurrentAssignment currentAssignment;
    private final List<AssignmentHistory> assignmentHistory;

    public DeviceDetailResponse(Device device,
                                 PatientDeviceAssignment currentAssignment,
                                 List<PatientDeviceAssignment> history) {
        this.id = device.getId();
        this.serialNumber = device.getSerialNumber();
        this.modelName = device.getModelName();
        this.deviceStatus = device.getDeviceStatus();
        this.batteryLevel = device.getBatteryLevel();
        this.lastSyncAt = device.getLastSyncAt();
        this.createdAt = device.getCreatedAt();
        this.currentAssignment = currentAssignment != null ? new CurrentAssignment(currentAssignment) : null;
        this.assignmentHistory = history.stream().map(AssignmentHistory::new).toList();
    }

    @Getter
    public static class CurrentAssignment {
        private final Long assignmentId;
        private final Long patientId;
        private final String patientCode;
        private final String patientName;
        private final LocalDate monitoringStartDate;
        private final AssignmentStatus assignmentStatus;

        public CurrentAssignment(PatientDeviceAssignment assignment) {
            this.assignmentId = assignment.getId();
            this.patientId = assignment.getPatient().getId();
            this.patientCode = assignment.getPatient().getPatientCode();
            this.patientName = assignment.getPatient().getName();
            this.monitoringStartDate = assignment.getMonitoringStartDate();
            this.assignmentStatus = assignment.getAssignmentStatus();
        }
    }

    @Getter
    public static class AssignmentHistory {
        private final Long assignmentId;
        private final String patientName;
        private final LocalDate monitoringStartDate;
        private final LocalDate monitoringEndDate;
        private final AssignmentStatus assignmentStatus;
        private final LocalDateTime assignedAt;
        private final LocalDateTime returnedAt;

        public AssignmentHistory(PatientDeviceAssignment assignment) {
            this.assignmentId = assignment.getId();
            this.patientName = assignment.getPatient().getName();
            this.monitoringStartDate = assignment.getMonitoringStartDate();
            this.monitoringEndDate = assignment.getMonitoringEndDate();
            this.assignmentStatus = assignment.getAssignmentStatus();
            this.assignedAt = assignment.getAssignedAt();
            this.returnedAt = assignment.getReturnedAt();
        }
    }
}
