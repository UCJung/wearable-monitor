package com.wearable.monitor.health

import android.content.Context
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SkinTemperatureRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.wearable.monitor.data.local.entity.BiometricEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    private val healthConnectClient: HealthConnectClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "HealthConnectManager"

        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(SkinTemperatureRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
    }

    // ── 권한 ──

    suspend fun checkPermissions(): Set<String> {
        return healthConnectClient.permissionController.getGrantedPermissions()
    }

    fun getPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    fun hasAllPermissions(granted: Set<String>): Boolean {
        return granted.containsAll(REQUIRED_PERMISSIONS)
    }

    // ── Health Connect 데이터 읽기 ──

    suspend fun readHeartRate(start: Instant, end: Instant): List<HeartRateRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read heart rate", e)
            emptyList()
        }
    }

    suspend fun readOxygenSaturation(start: Instant, end: Instant): List<OxygenSaturationRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read oxygen saturation", e)
            emptyList()
        }
    }

    suspend fun readSkinTemperature(start: Instant, end: Instant): List<SkinTemperatureRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SkinTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read skin temperature", e)
            emptyList()
        }
    }

    suspend fun readHeartRateVariability(start: Instant, end: Instant): List<HeartRateVariabilityRmssdRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read HRV", e)
            emptyList()
        }
    }

    suspend fun readRespiratoryRate(start: Instant, end: Instant): List<RespiratoryRateRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read respiratory rate", e)
            emptyList()
        }
    }

    suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read steps", e)
            emptyList()
        }
    }

    suspend fun readTotalCaloriesBurned(start: Instant, end: Instant): List<TotalCaloriesBurnedRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read calories burned", e)
            emptyList()
        }
    }

    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read exercise sessions", e)
            emptyList()
        }
    }

    suspend fun readSleepSessions(start: Instant, end: Instant): List<SleepSessionRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read sleep sessions", e)
            emptyList()
        }
    }

    // ── 전체 수집 통합 메서드 ──

    suspend fun collectAllData(lastSyncedAt: Instant): List<BiometricEntity> {
        val now = Instant.now()
        val start = lastSyncedAt
        val results = mutableListOf<BiometricEntity>()

        // 1. 심박수 (HEART_RATE)
        readHeartRate(start, now).forEach { record ->
            record.samples.forEach { sample ->
                results.add(
                    BiometricEntity(
                        itemCode = "HEART_RATE",
                        measuredAt = sample.time.toEpochMilli(),
                        valueNumeric = sample.beatsPerMinute.toDouble(),
                        category = "VITAL",
                        unit = "BPM"
                    )
                )
            }
        }

        // 2. 산소포화도 (SPO2)
        readOxygenSaturation(start, now).forEach { record ->
            results.add(
                BiometricEntity(
                    itemCode = "SPO2",
                    measuredAt = record.time.toEpochMilli(),
                    valueNumeric = record.percentage.value,
                    category = "VITAL",
                    unit = "%"
                )
            )
        }

        // 3. 피부 온도 (SKIN_TEMP)
        readSkinTemperature(start, now).forEach { record ->
            val baselineTemp = record.baseline?.inCelsius
            if (baselineTemp != null) {
                val deltas = record.deltas
                if (deltas.isNotEmpty()) {
                    for (delta in deltas) {
                        results.add(
                            BiometricEntity(
                                itemCode = "SKIN_TEMP",
                                measuredAt = delta.time.toEpochMilli(),
                                valueNumeric = baselineTemp + delta.delta.inCelsius,
                                category = "VITAL",
                                unit = "°C"
                            )
                        )
                    }
                } else {
                    results.add(
                        BiometricEntity(
                            itemCode = "SKIN_TEMP",
                            measuredAt = record.startTime.toEpochMilli(),
                            valueNumeric = baselineTemp,
                            category = "VITAL",
                            unit = "°C"
                        )
                    )
                }
            }
        }

        // 4. 심박변이도 HRV (HRV_RMSSD)
        readHeartRateVariability(start, now).forEach { record ->
            results.add(
                BiometricEntity(
                    itemCode = "HRV_RMSSD",
                    measuredAt = record.time.toEpochMilli(),
                    valueNumeric = record.heartRateVariabilityMillis,
                    category = "VITAL",
                    unit = "ms"
                )
            )
        }

        // 5. 호흡수 (RESPIRATORY_RATE)
        readRespiratoryRate(start, now).forEach { record ->
            results.add(
                BiometricEntity(
                    itemCode = "RESPIRATORY_RATE",
                    measuredAt = record.time.toEpochMilli(),
                    valueNumeric = record.rate,
                    category = "VITAL",
                    unit = "breaths/min"
                )
            )
        }

        // 6. 스트레스 — HRV 기반 추정 (STRESS)
        // 별도 Health Connect 레코드 없음. HRV 데이터 기반 앱단 계산 예정 (TASK-10).

        // 7. 걸음 수 (STEPS)
        readSteps(start, now).forEach { record ->
            results.add(
                BiometricEntity(
                    itemCode = "STEPS",
                    measuredAt = record.startTime.toEpochMilli(),
                    valueNumeric = record.count.toDouble(),
                    durationSec = ((record.endTime.epochSecond - record.startTime.epochSecond).toInt()),
                    category = "ACTIVITY",
                    unit = "steps"
                )
            )
        }

        // 8. 소모 칼로리 (CALORIES)
        readTotalCaloriesBurned(start, now).forEach { record ->
            results.add(
                BiometricEntity(
                    itemCode = "CALORIES",
                    measuredAt = record.startTime.toEpochMilli(),
                    valueNumeric = record.energy.inKilocalories,
                    durationSec = ((record.endTime.epochSecond - record.startTime.epochSecond).toInt()),
                    category = "ACTIVITY",
                    unit = "kcal"
                )
            )
        }

        // 9. 운동 시간 (EXERCISE)
        readExerciseSessions(start, now).forEach { record ->
            val durationSec = (record.endTime.epochSecond - record.startTime.epochSecond).toInt()
            results.add(
                BiometricEntity(
                    itemCode = "EXERCISE",
                    measuredAt = record.startTime.toEpochMilli(),
                    valueNumeric = (durationSec / 60.0),
                    valueText = record.exerciseType.toString(),
                    durationSec = durationSec,
                    category = "ACTIVITY",
                    unit = "min"
                )
            )
        }

        // 10~12. 수면 (SLEEP_DURATION, SLEEP_STAGES, SLEEP_SCORE)
        readSleepSessions(start, now).forEach { record ->
            val durationSec = (record.endTime.epochSecond - record.startTime.epochSecond).toInt()

            // 수면 시간
            results.add(
                BiometricEntity(
                    itemCode = "SLEEP_DURATION",
                    measuredAt = record.startTime.toEpochMilli(),
                    valueNumeric = (durationSec / 3600.0),
                    durationSec = durationSec,
                    category = "SLEEP",
                    unit = "hours"
                )
            )

            // 수면 단계 (JSON 텍스트)
            val stages = record.stages
            if (stages.isNotEmpty()) {
                val stagesJson = buildString {
                    append("[")
                    stages.forEachIndexed { index, stage ->
                        if (index > 0) append(",")
                        append("{\"stage\":${stage.stage}")
                        append(",\"startTime\":\"${stage.startTime}\"")
                        append(",\"endTime\":\"${stage.endTime}\"}")
                    }
                    append("]")
                }
                results.add(
                    BiometricEntity(
                        itemCode = "SLEEP_STAGES",
                        measuredAt = record.startTime.toEpochMilli(),
                        valueText = stagesJson,
                        durationSec = durationSec,
                        category = "SLEEP",
                        unit = null
                    )
                )
            }

            // 수면 점수 — Health Connect에 직접 점수 없음. 앱단 계산 예정 (TASK-10).
        }

        // 13~14. AI 종합 / 에너지 점수 — 앱단 계산 예정 (TASK-10)

        Log.i(TAG, "Collected ${results.size} biometric records from Health Connect")
        return results
    }
}
