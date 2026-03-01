package com.wearable.monitor.domain.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>, PatientRepositoryCustom {

    Optional<Patient> findByPatientCode(String patientCode);

    boolean existsByPatientCode(String patientCode);

    @Query(value = "SELECT nextval('patient_code_seq')", nativeQuery = true)
    Long nextCode();

    List<Patient> findByStatusNot(PatientStatus status);
}
