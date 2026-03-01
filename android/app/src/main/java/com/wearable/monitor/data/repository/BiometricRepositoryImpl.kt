package com.wearable.monitor.data.repository

import android.util.Log
import com.wearable.monitor.data.local.dao.BiometricDao
import com.wearable.monitor.data.local.dao.SyncStatusDao
import com.wearable.monitor.data.local.entity.BiometricEntity
import com.wearable.monitor.data.local.entity.SyncStatusEntity
import com.wearable.monitor.health.HealthConnectManager
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricRepositoryImpl @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val biometricDao: BiometricDao,
    private val syncStatusDao: SyncStatusDao
) : BiometricRepository {

    companion object {
        private const val TAG = "BiometricRepository"
        private const val DEFAULT_LOOKBACK_HOURS = 24L
    }

    override suspend fun collectAndBuffer(): Result<Int> {
        return try {
            // 모든 항목 중 가장 오래된 lastSyncedAt 기준으로 수집
            val allStatus = syncStatusDao.getAll()
            val lastSyncedAt = if (allStatus.isEmpty()) {
                Instant.now().minus(DEFAULT_LOOKBACK_HOURS, ChronoUnit.HOURS)
            } else {
                val oldestMillis = allStatus.minOf { it.lastSyncedAt }
                Instant.ofEpochMilli(oldestMillis)
            }

            val data = healthConnectManager.collectAllData(lastSyncedAt)

            if (data.isNotEmpty()) {
                biometricDao.insertAll(data)

                // 수집된 항목별로 lastSyncedAt 갱신
                val now = System.currentTimeMillis()
                data.map { it.itemCode }.distinct().forEach { itemCode ->
                    syncStatusDao.upsert(SyncStatusEntity(itemCode, now))
                }

                Log.i(TAG, "Collected and buffered ${data.size} records")
            }

            Result.success(data.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect and buffer data", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingData(): List<BiometricEntity> {
        return biometricDao.getPendingData()
    }

    override suspend fun markAsSynced(ids: List<Long>) {
        biometricDao.markAsSynced(ids)
    }

    override suspend fun cleanOldData(daysToKeep: Int) {
        val cutoff = Instant.now().minus(daysToKeep.toLong(), ChronoUnit.DAYS).toEpochMilli()
        biometricDao.deleteOldSyncedData(cutoff)
        Log.i(TAG, "Cleaned synced data older than $daysToKeep days")
    }
}
