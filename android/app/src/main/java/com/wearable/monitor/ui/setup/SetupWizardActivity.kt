package com.wearable.monitor.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wearable.monitor.R
import com.wearable.monitor.databinding.ActivitySetupWizardBinding
import com.wearable.monitor.ui.dashboard.DashboardActivity
import com.wearable.monitor.worker.SyncWorkerScheduler
import com.wearable.monitor.ui.setup.steps.Step1Fragment
import com.wearable.monitor.ui.setup.steps.Step2Fragment
import com.wearable.monitor.ui.setup.steps.Step3Fragment
import com.wearable.monitor.ui.setup.steps.Step4Fragment
import com.wearable.monitor.ui.setup.steps.Step5Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupWizardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupWizardBinding
    private val viewModel: SetupWizardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()

        // 초기 Fragment 표시
        if (savedInstanceState == null) {
            showStep(1)
        }
    }

    private fun setupListeners() {
        binding.btnPrevious.setOnClickListener {
            viewModel.previousStep()
        }

        binding.btnNext.setOnClickListener {
            val current = viewModel.currentStep.value
            if (current == SetupWizardViewModel.TOTAL_STEPS) {
                // 마지막 단계: 완료 처리
                if (viewModel.canComplete.value) {
                    navigateToDashboard()
                }
            } else {
                viewModel.nextStep()
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentStep.collect { step ->
                        showStep(step)
                        updateButtons(step)
                    }
                }

                launch {
                    viewModel.stepStatesList.collect { states ->
                        binding.stepIndicator.setSteps(states)
                    }
                }

                launch {
                    viewModel.canComplete.collect { canComplete ->
                        val isLastStep = viewModel.currentStep.value == SetupWizardViewModel.TOTAL_STEPS
                        if (isLastStep) {
                            binding.btnNext.isEnabled = canComplete
                        }
                    }
                }
            }
        }
    }

    private fun showStep(step: Int) {
        val fragment: Fragment = when (step) {
            1 -> Step1Fragment()
            2 -> Step2Fragment()
            3 -> Step3Fragment()
            4 -> Step4Fragment()
            5 -> Step5Fragment()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateButtons(step: Int) {
        binding.btnPrevious.visibility = if (step == 1) View.INVISIBLE else View.VISIBLE

        if (step == SetupWizardViewModel.TOTAL_STEPS) {
            binding.btnNext.text = getString(R.string.btn_complete)
            binding.btnNext.isEnabled = viewModel.canComplete.value
        } else {
            binding.btnNext.text = getString(R.string.btn_next)
            binding.btnNext.isEnabled = true
        }
    }

    private fun navigateToDashboard() {
        // Wizard 완료 시 SyncWorker 30분 주기 등록
        SyncWorkerScheduler.scheduleSync(this)

        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
