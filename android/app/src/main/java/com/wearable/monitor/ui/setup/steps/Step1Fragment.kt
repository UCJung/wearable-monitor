package com.wearable.monitor.ui.setup.steps

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wearable.monitor.R
import com.wearable.monitor.databinding.FragmentStep1Binding
import com.wearable.monitor.health.HealthConnectManager
import com.wearable.monitor.ui.setup.SetupWizardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Step1Fragment : Fragment() {

    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    @Inject lateinit var healthConnectManager: HealthConnectManager
    private val viewModel: SetupWizardViewModel by activityViewModels()

    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>

    private data class PermissionItem(val icon: String, val label: String, val permission: String)

    private val permissionItems = listOf(
        PermissionItem("❤️", "심박수", "android.permission.health.READ_HEART_RATE"),
        PermissionItem("💧", "산소포화도", "android.permission.health.READ_OXYGEN_SATURATION"),
        PermissionItem("🌡️", "피부 온도", "android.permission.health.READ_SKIN_TEMPERATURE"),
        PermissionItem("🚶", "걸음 수", "android.permission.health.READ_STEPS"),
        PermissionItem("😴", "수면", "android.permission.health.READ_SLEEP"),
        PermissionItem("💓", "심박변이도", "android.permission.health.READ_HEART_RATE_VARIABILITY"),
        PermissionItem("🫁", "호흡수", "android.permission.health.READ_RESPIRATORY_RATE"),
        PermissionItem("🔥", "소모 칼로리", "android.permission.health.READ_TOTAL_CALORIES_BURNED"),
        PermissionItem("🏃", "운동", "android.permission.health.READ_EXERCISE")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(
            healthConnectManager.getPermissionContract()
        ) { granted ->
            updatePermissionList(granted)
            if (healthConnectManager.hasAllPermissions(granted)) {
                viewModel.markStepCompleted(1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRequestPermissions.setOnClickListener {
            permissionLauncher.launch(HealthConnectManager.REQUIRED_PERMISSIONS)
        }

        // 초기 권한 상태 확인
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val granted = healthConnectManager.checkPermissions()
                updatePermissionList(granted)
                if (healthConnectManager.hasAllPermissions(granted)) {
                    viewModel.markStepCompleted(1)
                }
            }
        }
    }

    private fun updatePermissionList(granted: Set<String>) {
        binding.permissionList.removeAllViews()
        val dp = resources.displayMetrics.density

        for (item in permissionItems) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((11 * dp).toInt(), (11 * dp).toInt(), (11 * dp).toInt(), (11 * dp).toInt())
                setBackgroundColor(resources.getColor(R.color.bg_input, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * dp).toInt() }
            }

            val icon = TextView(requireContext()).apply {
                text = item.icon
                textSize = 20f
                setPadding(0, 0, (10 * dp).toInt(), 0)
            }

            val labelLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val label = TextView(requireContext()).apply {
                text = item.label
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_primary, null))
            }

            val permName = TextView(requireContext()).apply {
                text = item.permission.substringAfterLast(".")
                textSize = 11f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }

            labelLayout.addView(label)
            labelLayout.addView(permName)

            val isGranted = granted.any { it.contains(item.permission.substringAfterLast(".")) }
            val status = TextView(requireContext()).apply {
                if (isGranted) {
                    text = "✓"
                    setTextColor(resources.getColor(R.color.ok, null))
                } else {
                    text = getString(R.string.permission_required)
                    setTextColor(resources.getColor(R.color.primary, null))
                }
                textSize = 12f
            }

            row.addView(icon)
            row.addView(labelLayout)
            row.addView(status)
            binding.permissionList.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
