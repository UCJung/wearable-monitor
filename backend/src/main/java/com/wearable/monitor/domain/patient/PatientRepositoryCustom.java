package com.wearable.monitor.domain.patient;

import com.wearable.monitor.api.patient.dto.PatientSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientRepositoryCustom {

    Page<Patient> findByCondition(PatientSearchCondition condition, Pageable pageable);
}
