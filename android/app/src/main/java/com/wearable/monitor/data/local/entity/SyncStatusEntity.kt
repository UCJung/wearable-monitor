package com.wearable.monitor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_code")
    val itemCode: String,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long  // epoch millis
)
