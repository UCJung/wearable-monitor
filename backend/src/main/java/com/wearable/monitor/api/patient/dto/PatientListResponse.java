package com.wearable.monitor.api.patient.dto;

import com.wearable.monitor.domain.patient.Patient;
import com.wearable.monitor.domain.patient.PatientStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PatientListResponse {

    private final Long id;
    private final String patientCode;
    private final String name;
    private final LocalDate birthDate;
    private final String gender;
    private final PatientStatus status;
    private final boolean hasDevice;
    private final LocalDateTime createdAt;

    public PatientListResponse(Patient patient, boolean hasDevice) {
        this.id = patient.getId();
        this.patientCode = patient.getPatientCode();
        this.name = patient.getName();
        this.birthDate = patient.getBirthDate();
        this.gender = patient.getGender();
        this.status = patient.getStatus();
        this.hasDevice = hasDevice;
        this.createdAt = patient.getCreatedAt();
    }
}
