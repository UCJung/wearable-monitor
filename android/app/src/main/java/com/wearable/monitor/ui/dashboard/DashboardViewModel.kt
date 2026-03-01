package com.wearable.monitor.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wearable.monitor.data.local.dao.BiometricDao
import com.wearable.monitor.data.local.entity.BiometricEntity
import com.wearable.monitor.data.repository.BiometricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val biometricRepository: BiometricRepository,
    private val biometricDao: BiometricDao
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadData()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // HC에서 최신 데이터 수집
                biometricRepository.collectAndBuffer()
                loadData()
            } catch (e: Exception) {
                Log.e(TAG, "Refresh failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val date = _uiState.value.selectedDate
                val zone = ZoneId.systemDefault()
                val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

                val data = biometricDao.getByDateRange(startMillis, endMillis)

                val vitalData = buildVitalData(data)
                val activityData = buildActivityData(data)
                val sleepData = buildSleepData(data)
                val energyScore = calculateEnergyScore(vitalData, activityData, sleepData)

                _uiState.update {
                    it.copy(
                        vitalData = vitalData,
                        activityData = activityData,
                        sleepData = sleepData,
                        energyScore = energyScore,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildVitalData(data: List<BiometricEntity>): VitalData? {
        val heartRates = data.filter { it.itemCode == "HEART_RATE" }
        val spo2Records = data.filter { it.itemCode == "SPO2" }
        val skinTempRecords = data.filter { it.itemCode == "SKIN_TEMP" }
        val hrvRecords = data.filter { it.itemCode == "HRV_RMSSD" }
        val respRecords = data.filter { it.itemCode == "RESPIRATORY_RATE" }

        if (heartRates.isEmpty() && spo2Records.isEmpty() && skinTempRecords.isEmpty()) {
            return null
        }

        val hrValues = heartRates.mapNotNull { it.valueNumeric }

        return VitalData(
            heartRate = hrValues.lastOrNull(),
            heartRateMin = hrValues.minOrNull(),
            heartRateMax = hrValues.maxOrNull(),
            spo2 = spo2Records.lastOrNull()?.valueNumeric,
            skinTemp = skinTempRecords.lastOrNull()?.valueNumeric,
            stressLevel = calculateStress(hrvRecords),
            hrvRmssd = hrvRecords.lastOrNull()?.valueNumeric,
            respiratoryRate = respRecords.lastOrNull()?.valueNumeric
        )
    }

    private fun buildActivityData(data: List<BiometricEntity>): ActivityData? {
        val stepsRecords = data.filter { it.itemCode == "STEPS" }
        val caloriesRecords = data.filter { it.itemCode == "CALORIES" }
        val exerciseRecords = data.filter { it.itemCode == "EXERCISE" }

        if (stepsRecords.isEmpty() && caloriesRecords.isEmpty()) {
            return null
        }

        return ActivityData(
            steps = stepsRecords.sumOf { it.valueNumeric?.toLong() ?: 0L }.takeIf { it > 0 },
            calories = caloriesRecords.sumOf { it.valueNumeric ?: 0.0 }.takeIf { it > 0 },
            exerciseMinutes = exerciseRecords.sumOf { it.valueNumeric ?: 0.0 }.takeIf { it > 0 }
        )
    }

    private fun buildSleepData(data: List<BiometricEntity>): SleepData? {
        val sleepDuration = data.filter { it.itemCode == "SLEEP_DURATION" }
        val sleepStages = data.filter { it.itemCode == "SLEEP_STAGES" }

        if (sleepDuration.isEmpty()) return null

        val totalHours = sleepDuration.lastOrNull()?.valueNumeric
        val durationSec = sleepDuration.lastOrNull()?.durationSec ?: 0

        // 수면 단계 비율 (간이 파싱)
        var deepPct = 0f
        var remPct = 0f
        var lightPct = 0f

        val stagesText = sleepStages.lastOrNull()?.valueText
        if (!stagesText.isNullOrEmpty() && durationSec > 0) {
            // stage 값: 1=AWAKE, 2=SLEEPING, 3=OUT_OF_BED, 4=LIGHT, 5=DEEP, 6=REM
            val deepCount = stagesText.count { it == '5' }   // "stage":5
            val remCount = stagesText.count { it == '6' }    // "stage":6
            val total = (deepCount + remCount + stagesText.count { it == '4' }).coerceAtLeast(1)
            deepPct = deepCount.toFloat() / total
            remPct = remCount.toFloat() / total
            lightPct = 1f - deepPct - remPct
        } else {
            // 기본 비율
            deepPct = 0.2f
            remPct = 0.25f
            lightPct = 0.55f
        }

        val sleepScore = totalHours?.let { hours ->
            // 간이 점수: 7-9시간 최적, HRV 반영
            when {
                hours >= 7.0 && hours <= 9.0 -> (70 + (deepPct * 30)).toInt().coerceAtMost(100)
                hours >= 6.0 -> (55 + (deepPct * 25)).toInt()
                else -> (40 + (deepPct * 20)).toInt()
            }
        }

        return SleepData(
            durationHours = totalHours,
            sleepScore = sleepScore,
            deepSleepPct = deepPct,
            remSleepPct = remPct,
            lightSleepPct = lightPct
        )
    }

    /** HRV 기반 스트레스 추정 (0~100, 높을수록 스트레스 높음) */
    private fun calculateStress(hrvRecords: List<BiometricEntity>): Double? {
        val avgHrv = hrvRecords.mapNotNull { it.valueNumeric }.average().takeIf { !it.isNaN() }
            ?: return null
        // HRV 높으면 스트레스 낮음 (역상관)
        return (100 - (avgHrv / 1.5)).coerceIn(0.0, 100.0)
    }

    /** 에너지 점수 종합 계산 (0~100) */
    private fun calculateEnergyScore(
        vital: VitalData?,
        activity: ActivityData?,
        sleep: SleepData?
    ): Int? {
        if (vital == null && activity == null && sleep == null) return null

        var score = 50.0
        var factors = 0

        // 수면 점수 반영 (40%)
        sleep?.sleepScore?.let {
            score += (it - 50) * 0.4
            factors++
        }

        // 활동량 반영 (30%)
        activity?.steps?.let { steps ->
            val stepScore = (steps.toDouble() / activity.stepsGoal * 100).coerceAtMost(100.0)
            score += (stepScore - 50) * 0.3
            factors++
        }

        // HRV 반영 (30%)
        vital?.hrvRmssd?.let { hrv ->
            val hrvScore = (hrv / 1.0).coerceAtMost(100.0)
            score += (hrvScore - 50) * 0.3
            factors++
        }

        return if (factors > 0) score.toInt().coerceIn(0, 100) else null
    }
}
