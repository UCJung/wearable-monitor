package com.wearable.monitor.domain.biometric;

import com.wearable.monitor.domain.assignment.PatientDeviceAssignment;
import com.wearable.monitor.domain.itemdef.CollectionItemDefinition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_biometric_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class PatientBiometricHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private PatientDeviceAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_def_id", nullable = false)
    private CollectionItemDefinition itemDef;

    // 조회 최적화용 — FK 조인 없이 직접 필터링
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    // TimescaleDB 파티션 키
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "value_numeric", precision = 12, scale = 4)
    private BigDecimal valueNumeric;

    @Column(name = "value_text")
    private String valueText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value_json", columnDefinition = "jsonb")
    private String valueJson;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PatientBiometricHistory(PatientDeviceAssignment assignment,
                                   CollectionItemDefinition itemDef,
                                   LocalDateTime measuredAt,
                                   BigDecimal valueNumeric,
                                   String valueText,
                                   String valueJson,
                                   Integer durationSec) {
        this.assignment = assignment;
        this.itemDef = itemDef;
        this.itemCode = itemDef.getItemCode();
        this.measuredAt = measuredAt;
        this.valueNumeric = valueNumeric;
        this.valueText = valueText;
        this.valueJson = valueJson;
        this.durationSec = durationSec;
    }
}
