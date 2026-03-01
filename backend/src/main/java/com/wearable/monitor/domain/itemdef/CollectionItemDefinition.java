package com.wearable.monitor.domain.itemdef;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "collection_item_definitions")
@Getter
@NoArgsConstructor
public class CollectionItemDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", unique = true, nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name_ko", nullable = false, length = 100)
    private String itemNameKo;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_mode", nullable = false, length = 20)
    private CollectionMode collectionMode;

    @Column(name = "hc_record_type", length = 100)
    private String hcRecordType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "collection_interval_desc", length = 100)
    private String collectionIntervalDesc;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public CollectionItemDefinition(String itemCode, String itemNameKo, ItemCategory category,
                                    CollectionMode collectionMode, String hcRecordType,
                                    String unit, String collectionIntervalDesc, Integer displayOrder) {
        this.itemCode = itemCode;
        this.itemNameKo = itemNameKo;
        this.category = category;
        this.collectionMode = collectionMode;
        this.hcRecordType = hcRecordType;
        this.unit = unit;
        this.collectionIntervalDesc = collectionIntervalDesc;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }
}
