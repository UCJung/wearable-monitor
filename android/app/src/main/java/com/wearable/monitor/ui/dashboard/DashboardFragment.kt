package com.wearable.monitor.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wearable.monitor.R
import com.wearable.monitor.databinding.FragmentDashboardBinding
import com.wearable.monitor.databinding.ItemDataCardBinding
import com.wearable.monitor.databinding.ItemEnergyCardBinding
import com.wearable.monitor.databinding.ItemSleepCardBinding
import com.wearable.monitor.databinding.ItemStepsCardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREAN)
    private val decimalFormat = DecimalFormat("#.#")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: DashboardUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading

        // Header
        binding.tvTitle.text = getString(R.string.dashboard_greeting)
        binding.tvDate.text = state.selectedDate.format(dateFormatter)

        // Connection badge
        if (state.watchConnected) {
            binding.tvConnectionStatus.text = getString(R.string.connection_connected)
            binding.dotConnection.setBackgroundColor(0xFF4ADE80.toInt())
        } else {
            binding.tvConnectionStatus.text = getString(R.string.connection_disconnected)
            binding.dotConnection.setBackgroundColor(0xFFFF6B6B.toInt())
        }

        // Last sync
        if (state.lastSyncAt.isNotEmpty()) {
            binding.tvLastSync.text = getString(R.string.last_sync_format, state.lastSyncAt)
        }

        // Data cards
        updateVitalCards(state.vitalData)
        updateActivityCards(state.activityData)
        updateSleepCard(state.sleepData)
        updateEnergyCard(state.energyScore)
        updateWarnings(state)
    }

    private fun updateVitalCards(data: VitalData?) {
        // Heart Rate
        setupDataCard(
            binding.cardHeartRate, "❤️",
            getString(R.string.card_heart_rate), getString(R.string.unit_bpm),
            data?.heartRate?.toInt()?.toString(),
            if (data?.heartRateMin != null && data.heartRateMax != null)
                getString(R.string.hr_range_format,
                    data.heartRateMin.toInt().toString(),
                    data.heartRateMax.toInt().toString())
            else null
        )

        // SpO2
        setupDataCard(
            binding.cardSpo2, "🫁",
            getString(R.string.card_spo2), getString(R.string.unit_percent),
            data?.spo2?.toInt()?.toString(), null
        )

        // Stress
        setupDataCard(
            binding.cardStress, "😰",
            getString(R.string.card_stress), "",
            data?.stressLevel?.toInt()?.toString(), null
        )

        // Skin Temp
        setupDataCard(
            binding.cardSkinTemp, "🌡️",
            getString(R.string.card_skin_temp), getString(R.string.unit_celsius),
            data?.skinTemp?.let { decimalFormat.format(it) }, null
        )
    }

    private fun setupDataCard(
        card: ItemDataCardBinding,
        icon: String,
        label: String,
        unit: String,
        value: String?,
        subText: String?
    ) {
        card.tvIcon.text = icon
        card.tvLabel.text = label
        card.tvUnit.text = unit
        if (value != null) {
            card.root.alpha = 1f
            card.tvValue.text = value
            if (subText != null) {
                card.tvSub.text = subText
                card.tvSub.visibility = View.VISIBLE
            } else {
                card.tvSub.visibility = View.GONE
            }
        } else {
            card.root.alpha = 0.5f
            card.tvValue.text = getString(R.string.no_data)
            card.tvSub.visibility = View.GONE
        }
    }

    private fun updateActivityCards(data: ActivityData?) {
        // Steps card
        val stepsCard = binding.cardSteps
        if (data?.steps != null) {
            stepsCard.root.alpha = 1f
            stepsCard.tvStepsValue.text = numberFormat.format(data.steps)
            stepsCard.tvStepsGoal.text =
                getString(R.string.steps_goal_format, numberFormat.format(data.stepsGoal))

            val pct = (data.steps.toFloat() / data.stepsGoal).coerceAtMost(1f)
            stepsCard.viewStepsProgress.post {
                val parent = stepsCard.viewStepsProgress.parent as View
                val params = stepsCard.viewStepsProgress.layoutParams
                params.width = (parent.width * pct).toInt()
                stepsCard.viewStepsProgress.layoutParams = params
            }
            stepsCard.tvStepsPct.text = String.format(Locale.US, "%.1f%%", pct * 100)
        } else {
            stepsCard.root.alpha = 0.5f
            stepsCard.tvStepsValue.text = getString(R.string.no_data)
            stepsCard.tvStepsPct.text = getString(R.string.no_data_desc)
        }

        // Calories
        setupDataCard(
            binding.cardCalories, "🔥",
            getString(R.string.card_calories), getString(R.string.unit_kcal),
            data?.calories?.toInt()?.toString(), null
        )

        // Exercise
        setupDataCard(
            binding.cardExercise, "⏱️",
            getString(R.string.card_exercise), getString(R.string.unit_minutes),
            data?.exerciseMinutes?.toInt()?.toString(), null
        )
    }

    private fun updateSleepCard(data: SleepData?) {
        val sleepCard = binding.cardSleep
        if (data != null) {
            sleepCard.root.alpha = 1f
            sleepCard.tvSleepValue.text = decimalFormat.format(data.durationHours ?: 0.0)

            data.sleepScore?.let { score ->
                sleepCard.tvSleepScore.text = getString(R.string.sleep_score_format, score)
            }

            // Update stage bar weights
            (sleepCard.viewDeepSleep.layoutParams as LinearLayout.LayoutParams).weight =
                data.deepSleepPct * 100
            (sleepCard.viewRemSleep.layoutParams as LinearLayout.LayoutParams).weight =
                data.remSleepPct * 100
            (sleepCard.viewLightSleep.layoutParams as LinearLayout.LayoutParams).weight =
                data.lightSleepPct * 100
            sleepCard.viewDeepSleep.requestLayout()

            sleepCard.tvDeepPct.text =
                getString(R.string.deep_sleep_pct, (data.deepSleepPct * 100).toInt())
            sleepCard.tvRemPct.text =
                getString(R.string.rem_sleep_pct, (data.remSleepPct * 100).toInt())
            sleepCard.tvLightPct.text =
                getString(R.string.light_sleep_pct, (data.lightSleepPct * 100).toInt())
        } else {
            sleepCard.root.alpha = 0.5f
            sleepCard.tvSleepValue.text = getString(R.string.no_data)
            sleepCard.tvSleepScore.text = ""
        }
    }

    private fun updateEnergyCard(score: Int?) {
        val energyCard = binding.cardEnergy
        if (score != null) {
            energyCard.root.alpha = 1f
            energyCard.energyGauge.setScore(score)
            energyCard.tvEnergyValue.text = getString(R.string.energy_value_format, score)
            energyCard.tvEnergyDesc.text = when {
                score >= 80 -> getString(R.string.energy_good)
                score >= 50 -> getString(R.string.energy_normal)
                else -> getString(R.string.energy_low)
            }
        } else {
            energyCard.root.alpha = 0.5f
            energyCard.energyGauge.setScore(0)
            energyCard.tvEnergyValue.text = getString(R.string.no_data)
            energyCard.tvEnergyDesc.text = getString(R.string.no_data_desc)
        }
    }

    private fun updateWarnings(state: DashboardUiState) {
        binding.warningContainer.removeAllViews()

        val showWarnings = !state.watchConnected || state.batteryLevel < 20

        if (!state.watchConnected) {
            val banner = layoutInflater.inflate(
                R.layout.item_warning_banner, binding.warningContainer, false
            )
            banner.setBackgroundResource(R.drawable.bg_warn_card)
            banner.findViewById<TextView>(R.id.tvWarningIcon).text = "⚠️"
            banner.findViewById<TextView>(R.id.tvWarningTitle).text =
                getString(R.string.warn_no_data_title)
            banner.findViewById<TextView>(R.id.tvWarningDesc).text =
                getString(R.string.warn_no_data_desc)
            binding.warningContainer.addView(banner)
            binding.headerLayout.setBackgroundResource(R.drawable.bg_header_gradient_warn)
        } else {
            binding.headerLayout.setBackgroundResource(R.drawable.bg_header_gradient)
        }

        if (state.batteryLevel < 20) {
            val banner = layoutInflater.inflate(
                R.layout.item_warning_banner, binding.warningContainer, false
            )
            banner.setBackgroundResource(R.drawable.bg_danger_card)
            banner.findViewById<TextView>(R.id.tvWarningIcon).text = "🪫"
            banner.findViewById<TextView>(R.id.tvWarningTitle).text =
                getString(R.string.warn_battery_title)
            banner.findViewById<TextView>(R.id.tvWarningDesc).text =
                getString(R.string.warn_battery_desc)
            binding.warningContainer.addView(banner)
        }

        binding.warningContainer.isVisible = showWarnings
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
