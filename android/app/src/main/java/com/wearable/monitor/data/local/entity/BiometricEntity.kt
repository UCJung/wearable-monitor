package com.wearable.monitor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biometric_buffer")
data class BiometricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "item_code")
    val itemCode: String,

    @ColumnInfo(name = "measured_at")
    val measuredAt: Long,  // epoch millis

    @ColumnInfo(name = "value_numeric")
    val valueNumeric: Double? = null,

    @ColumnInfo(name = "value_text")
    val valueText: String? = null,

    @ColumnInfo(name = "duration_sec")
    val durationSec: Int? = null,

    val category: String,

    val unit: String? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
