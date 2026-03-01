package com.wearable.monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wearable.monitor.data.local.entity.BiometricEntity

@Dao
interface BiometricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BiometricEntity>)

    @Query("SELECT * FROM biometric_buffer WHERE is_synced = 0 LIMIT 500")
    suspend fun getPendingData(): List<BiometricEntity>

    @Query("UPDATE biometric_buffer SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM biometric_buffer WHERE is_synced = 1 AND created_at < :before")
    suspend fun deleteOldSyncedData(before: Long)

    @Query("SELECT * FROM biometric_buffer WHERE measured_at >= :startMillis AND measured_at < :endMillis ORDER BY measured_at DESC")
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<BiometricEntity>

    @Query("SELECT * FROM biometric_buffer WHERE item_code = :itemCode AND measured_at >= :startMillis AND measured_at < :endMillis ORDER BY measured_at DESC")
    suspend fun getByItemCodeAndDateRange(itemCode: String, startMillis: Long, endMillis: Long): List<BiometricEntity>
}
