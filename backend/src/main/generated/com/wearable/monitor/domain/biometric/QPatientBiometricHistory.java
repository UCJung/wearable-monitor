package com.wearable.monitor.domain.biometric;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPatientBiometricHistory is a Querydsl query type for PatientBiometricHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPatientBiometricHistory extends EntityPathBase<PatientBiometricHistory> {

    private static final long serialVersionUID = -147255363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPatientBiometricHistory patientBiometricHistory = new QPatientBiometricHistory("patientBiometricHistory");

    public final com.wearable.monitor.domain.assignment.QPatientDeviceAssignment assignment;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> durationSec = createNumber("durationSec", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemCode = createString("itemCode");

    public final com.wearable.monitor.domain.itemdef.QCollectionItemDefinition itemDef;

    public final DateTimePath<java.time.LocalDateTime> measuredAt = createDateTime("measuredAt", java.time.LocalDateTime.class);

    public final StringPath valueJson = createString("valueJson");

    public final NumberPath<java.math.BigDecimal> valueNumeric = createNumber("valueNumeric", java.math.BigDecimal.class);

    public final StringPath valueText = createString("valueText");

    public QPatientBiometricHistory(String variable) {
        this(PatientBiometricHistory.class, forVariable(variable), INITS);
    }

    public QPatientBiometricHistory(Path<? extends PatientBiometricHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPatientBiometricHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPatientBiometricHistory(PathMetadata metadata, PathInits inits) {
        this(PatientBiometricHistory.class, metadata, inits);
    }

    public QPatientBiometricHistory(Class<? extends PatientBiometricHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.assignment = inits.isInitialized("assignment") ? new com.wearable.monitor.domain.assignment.QPatientDeviceAssignment(forProperty("assignment"), inits.get("assignment")) : null;
        this.itemDef = inits.isInitialized("itemDef") ? new com.wearable.monitor.domain.itemdef.QCollectionItemDefinition(forProperty("itemDef")) : null;
    }

}

