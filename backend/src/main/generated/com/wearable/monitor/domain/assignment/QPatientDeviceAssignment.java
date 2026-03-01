package com.wearable.monitor.domain.assignment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPatientDeviceAssignment is a Querydsl query type for PatientDeviceAssignment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPatientDeviceAssignment extends EntityPathBase<PatientDeviceAssignment> {

    private static final long serialVersionUID = -1190528075L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPatientDeviceAssignment patientDeviceAssignment = new QPatientDeviceAssignment("patientDeviceAssignment");

    public final DateTimePath<java.time.LocalDateTime> assignedAt = createDateTime("assignedAt", java.time.LocalDateTime.class);

    public final EnumPath<AssignmentStatus> assignmentStatus = createEnum("assignmentStatus", AssignmentStatus.class);

    public final com.wearable.monitor.domain.device.QDevice device;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> monitoringEndDate = createDate("monitoringEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> monitoringStartDate = createDate("monitoringStartDate", java.time.LocalDate.class);

    public final com.wearable.monitor.domain.patient.QPatient patient;

    public final DateTimePath<java.time.LocalDateTime> returnedAt = createDateTime("returnedAt", java.time.LocalDateTime.class);

    public QPatientDeviceAssignment(String variable) {
        this(PatientDeviceAssignment.class, forVariable(variable), INITS);
    }

    public QPatientDeviceAssignment(Path<? extends PatientDeviceAssignment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPatientDeviceAssignment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPatientDeviceAssignment(PathMetadata metadata, PathInits inits) {
        this(PatientDeviceAssignment.class, metadata, inits);
    }

    public QPatientDeviceAssignment(Class<? extends PatientDeviceAssignment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.device = inits.isInitialized("device") ? new com.wearable.monitor.domain.device.QDevice(forProperty("device")) : null;
        this.patient = inits.isInitialized("patient") ? new com.wearable.monitor.domain.patient.QPatient(forProperty("patient")) : null;
    }

}

