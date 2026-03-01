package com.wearable.monitor.ui.setup.steps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wearable.monitor.R
import com.wearable.monitor.databinding.FragmentStep5Binding
import com.wearable.monitor.ui.setup.SetupWizardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Step5Fragment : Fragment() {

    private var _binding: FragmentStep5Binding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupWizardViewModel by activityViewModels()

    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkBatteryOptimization()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDisableOptimization.setOnClickListener {
            requestBatteryOptimizationExemption()
        }

        checkBatteryOptimization()
    }

    override fun onResume() {
        super.onResume()
        checkBatteryOptimization()
    }

    private fun requestBatteryOptimizationExemption() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        batteryOptimizationLauncher.launch(intent)
    }

    private fun checkBatteryOptimization() {
        val powerManager = requireContext().getSystemService<PowerManager>()
        val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(requireContext().packageName) == true

        if (isIgnoring) {
            binding.tvBatteryStatus.text = getString(R.string.optimization_disabled)
            binding.tvBatteryStatus.setTextColor(resources.getColor(R.color.ok, null))
            binding.statusDot.setBackgroundColor(resources.getColor(R.color.ok, null))
            binding.btnDisableOptimization.isEnabled = false
            binding.btnDisableOptimization.text = getString(R.string.optimization_disabled)
            viewModel.markStepCompleted(5)
        } else {
            binding.tvBatteryStatus.text = getString(R.string.optimization_enabled)
            binding.tvBatteryStatus.setTextColor(resources.getColor(R.color.warn, null))
            binding.statusDot.setBackgroundColor(resources.getColor(R.color.warn, null))
            binding.btnDisableOptimization.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
