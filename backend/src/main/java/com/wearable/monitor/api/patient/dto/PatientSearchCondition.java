package com.wearable.monitor.api.patient.dto;

import com.wearable.monitor.domain.patient.PatientStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientSearchCondition {

    private String name;
    private String patientCode;
    private PatientStatus status;
    private Boolean hasDevice;
}
