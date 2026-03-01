package com.wearable.monitor.domain.device;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDevice is a Querydsl query type for Device
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDevice extends EntityPathBase<Device> {

    private static final long serialVersionUID = -1564974272L;

    public static final QDevice device = new QDevice("device");

    public final NumberPath<Integer> batteryLevel = createNumber("batteryLevel", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<DeviceStatus> deviceStatus = createEnum("deviceStatus", DeviceStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastSyncAt = createDateTime("lastSyncAt", java.time.LocalDateTime.class);

    public final StringPath modelName = createString("modelName");

    public final StringPath serialNumber = createString("serialNumber");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QDevice(String variable) {
        super(Device.class, forVariable(variable));
    }

    public QDevice(Path<? extends Device> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDevice(PathMetadata metadata) {
        super(Device.class, metadata);
    }

}

