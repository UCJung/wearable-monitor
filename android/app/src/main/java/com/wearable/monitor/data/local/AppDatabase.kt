package com.wearable.monitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wearable.monitor.data.local.dao.BiometricDao
import com.wearable.monitor.data.local.dao.SyncStatusDao
import com.wearable.monitor.data.local.entity.BiometricEntity
import com.wearable.monitor.data.local.entity.SyncStatusEntity

@Database(
    entities = [BiometricEntity::class, SyncStatusEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun biometricDao(): BiometricDao
    abstract fun syncStatusDao(): SyncStatusDao
}
