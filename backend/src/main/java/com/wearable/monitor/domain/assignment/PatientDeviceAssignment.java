package com.wearable.monitor.domain.assignment;

import com.wearable.monitor.domain.device.Device;
import com.wearable.monitor.domain.patient.Patient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_device_assignments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class PatientDeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 20)
    private AssignmentStatus assignmentStatus = AssignmentStatus.ACTIVE;

    @Column(name = "monitoring_start_date")
    private LocalDate monitoringStartDate;

    @Column(name = "monitoring_end_date")
    private LocalDate monitoringEndDate;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    public PatientDeviceAssignment(Patient patient, Device device, LocalDate monitoringStartDate) {
        this.patient = patient;
        this.device = device;
        this.monitoringStartDate = monitoringStartDate;
        this.assignmentStatus = AssignmentStatus.ACTIVE;
    }

    public void returnDevice(LocalDate monitoringEndDate) {
        this.assignmentStatus = AssignmentStatus.RETURNED;
        this.monitoringEndDate = monitoringEndDate;
        this.returnedAt = LocalDateTime.now();
    }

    public void markAsLost() {
        this.assignmentStatus = AssignmentStatus.LOST;
        this.returnedAt = LocalDateTime.now();
    }
}
