package com.wearable.monitor.domain.patient;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPatient is a Querydsl query type for Patient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPatient extends EntityPathBase<Patient> {

    private static final long serialVersionUID = -1952947538L;

    public static final QPatient patient = new QPatient("patient");

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath notes = createString("notes");

    public final StringPath patientCode = createString("patientCode");

    public final EnumPath<PatientStatus> status = createEnum("status", PatientStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QPatient(String variable) {
        super(Patient.class, forVariable(variable));
    }

    public QPatient(Path<? extends Patient> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPatient(PathMetadata metadata) {
        super(Patient.class, metadata);
    }

}

