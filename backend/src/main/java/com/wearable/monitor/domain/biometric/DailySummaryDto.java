package com.wearable.monitor.domain.biometric;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DailySummaryDto {

    private final Long patientId;
    private final String itemCode;
    private final LocalDate measureDate;
    private final Double avgValue;        // QueryDSL avg()는 Double 반환
    private final BigDecimal minValue;
    private final BigDecimal maxValue;
    private final Long measureCount;

    // QueryDSL Projections.constructor 호출용 — LocalDate 직접 매핑
    public DailySummaryDto(Long patientId, String itemCode, LocalDate measureDate,
                           Double avgValue, BigDecimal minValue, BigDecimal maxValue,
                           Long measureCount) {
        this.patientId = patientId;
        this.itemCode = itemCode;
        this.measureDate = measureDate;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.measureCount = measureCount;
    }

    // CAST({0} AS DATE)가 java.sql.Date 반환 시 사용되는 생성자
    public DailySummaryDto(Long patientId, String itemCode, java.sql.Date measureDate,
                           Double avgValue, BigDecimal minValue, BigDecimal maxValue,
                           Long measureCount) {
        this.patientId = patientId;
        this.itemCode = itemCode;
        this.measureDate = measureDate != null ? measureDate.toLocalDate() : null;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.measureCount = measureCount;
    }
}
