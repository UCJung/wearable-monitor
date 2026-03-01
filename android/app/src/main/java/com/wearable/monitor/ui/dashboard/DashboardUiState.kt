package com.wearable.monitor.ui.dashboard

import java.time.LocalDate

data class DashboardUiState(
    val patientName: String = "",
    val watchConnected: Boolean = false,
    val lastSyncAt: String = "",
    val lastSyncMinutesAgo: Long = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val vitalData: VitalData? = null,
    val activityData: ActivityData? = null,
    val sleepData: SleepData? = null,
    val energyScore: Int? = null,
    val isLoading: Boolean = false,
    val isDeviceAssigned: Boolean = true,
    val hasPermissions: Boolean = true,
    val batteryLevel: Int = 100
)

data class VitalData(
    val heartRate: Double? = null,
    val heartRateMin: Double? = null,
    val heartRateMax: Double? = null,
    val spo2: Double? = null,
    val skinTemp: Double? = null,
    val stressLevel: Double? = null,
    val hrvRmssd: Double? = null,
    val respiratoryRate: Double? = null
)

data class ActivityData(
    val steps: Long? = null,
    val stepsGoal: Long = 10000,
    val calories: Double? = null,
    val exerciseMinutes: Double? = null
)

data class SleepData(
    val durationHours: Double? = null,
    val sleepScore: Int? = null,
    val deepSleepPct: Float = 0f,
    val remSleepPct: Float = 0f,
    val lightSleepPct: Float = 0f
)
