package com.wearable.monitor.domain.itemdef;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCollectionItemDefinition is a Querydsl query type for CollectionItemDefinition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCollectionItemDefinition extends EntityPathBase<CollectionItemDefinition> {

    private static final long serialVersionUID = -298334706L;

    public static final QCollectionItemDefinition collectionItemDefinition = new QCollectionItemDefinition("collectionItemDefinition");

    public final EnumPath<ItemCategory> category = createEnum("category", ItemCategory.class);

    public final StringPath collectionIntervalDesc = createString("collectionIntervalDesc");

    public final EnumPath<CollectionMode> collectionMode = createEnum("collectionMode", CollectionMode.class);

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final StringPath hcRecordType = createString("hcRecordType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath itemCode = createString("itemCode");

    public final StringPath itemNameKo = createString("itemNameKo");

    public final StringPath unit = createString("unit");

    public QCollectionItemDefinition(String variable) {
        super(CollectionItemDefinition.class, forVariable(variable));
    }

    public QCollectionItemDefinition(Path<? extends CollectionItemDefinition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCollectionItemDefinition(PathMetadata metadata) {
        super(CollectionItemDefinition.class, metadata);
    }

}

