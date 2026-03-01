package com.wearable.monitor.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wearable.monitor.data.local.entity.BiometricEntity
import com.wearable.monitor.data.remote.WearableApiService
import com.wearable.monitor.data.remote.dto.BatchUploadRequest
import com.wearable.monitor.data.remote.dto.BiometricUploadItem
import com.wearable.monitor.data.repository.BiometricRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val biometricRepository: BiometricRepository,
    private val apiService: WearableApiService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_BATCH_SIZE = 500
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "SyncWorker started (attempt: $runAttemptCount)")

        try {
            // 1. Health Connect에서 데이터 수집 → Room 저장
            val collectResult = biometricRepository.collectAndBuffer()
            collectResult.onFailure { e ->
                Log.w(TAG, "HC collect failed, continuing with pending data", e)
            }
            collectResult.onSuccess { count ->
                Log.i(TAG, "Collected $count records from HC")
            }

            // 2. 미전송 데이터 서버 업로드
            val pendingData = biometricRepository.getPendingData()
            if (pendingData.isNotEmpty()) {
                val uploadSuccess = uploadToServer(pendingData)
                if (uploadSuccess) {
                    biometricRepository.markAsSynced(pendingData.map { it.id })
                    Log.i(TAG, "Uploaded and marked ${pendingData.size} records")
                } else {
                    Log.w(TAG, "Upload failed, will retry")
                    return if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            } else {
                Log.i(TAG, "No pending data to upload")
            }

            // 3. 오래된 동기화 완료 데이터 삭제
            biometricRepository.cleanOldData(7)

            Log.i(TAG, "SyncWorker completed successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker failed", e)
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun uploadToServer(data: List<BiometricEntity>): Boolean {
        return try {
            val items = data.map { entity ->
                BiometricUploadItem(
                    itemCode = entity.itemCode,
                    measuredAt = Instant.ofEpochMilli(entity.measuredAt)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    valueNumeric = entity.valueNumeric,
                    valueText = entity.valueText,
                    durationSec = entity.durationSec,
                    category = entity.category,
                    unit = entity.unit
                )
            }

            val request = BatchUploadRequest(items = items)
            val response = apiService.uploadBatch(request)

            if (response.isSuccessful && response.body()?.code == "SUCCESS") {
                Log.i(TAG, "Batch upload success: ${response.body()?.data}")
                true
            } else {
                Log.w(TAG, "Batch upload failed: ${response.code()} - ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch upload exception", e)
            false
        }
    }
}
