package com.wearable.monitor.domain.patient;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_code", unique = true, nullable = false, length = 20)
    private String patientCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PatientStatus status = PatientStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Patient(String patientCode, String name, LocalDate birthDate, String gender, String notes) {
        this.patientCode = patientCode;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.notes = notes;
        this.status = PatientStatus.ACTIVE;
    }

    public void update(String name, LocalDate birthDate, String gender, String notes) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.notes = notes;
    }

    public void changeStatus(PatientStatus status) {
        this.status = status;
    }
}
