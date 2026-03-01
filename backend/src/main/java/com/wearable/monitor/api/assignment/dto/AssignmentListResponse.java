package com.wearable.monitor.api.assignment.dto;

import com.wearable.monitor.domain.assignment.AssignmentStatus;
import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class AssignmentListResponse {

    private final Long id;
    private final Long patientId;
    private final String patientCode;
    private final String patientName;
    private final Long deviceId;
    private final String serialNumber;
    private final String modelName;
    private final AssignmentStatus assignmentStatus;
    private final LocalDate monitoringStartDate;
    private final LocalDate monitoringEndDate;
    private final LocalDateTime assignedAt;
    private final LocalDateTime returnedAt;

    public AssignmentListResponse(PatientDeviceAssignment assignment) {
        this.id = assignment.getId();
        this.patientId = assignment.getPatient().getId();
        this.patientCode = assignment.getPatient().getPatientCode();
        this.patientName = assignment.getPatient().getName();
        this.deviceId = assignment.getDevice().getId();
        this.serialNumber = assignment.getDevice().getSerialNumber();
        this.modelName = assignment.getDevice().getModelName();
        this.assignmentStatus = assignment.getAssignmentStatus();
        this.monitoringStartDate = assignment.getMonitoringStartDate();
        this.monitoringEndDate = assignment.getMonitoringEndDate();
        this.assignedAt = assignment.getAssignedAt();
        this.returnedAt = assignment.getReturnedAt();
    }
}
