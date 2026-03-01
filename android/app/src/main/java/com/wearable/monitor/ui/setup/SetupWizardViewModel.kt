package com.wearable.monitor.ui.setup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SetupWizardViewModel @Inject constructor() : ViewModel() {

    companion object {
        const val TOTAL_STEPS = 5
        val REQUIRED_STEPS = setOf(1, 2, 5)  // 필수 단계
    }

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _completedSteps = MutableStateFlow<Set<Int>>(emptySet())
    val completedSteps: StateFlow<Set<Int>> = _completedSteps.asStateFlow()

    val stepStates: StateFlow<List<StepState>>
        get() = _currentStep.combine(_completedSteps) { current, completed ->
            (1..TOTAL_STEPS).map { step ->
                when {
                    step in completed -> StepState.DONE
                    step == current -> StepState.CURRENT
                    else -> StepState.TODO
                }
            }
        }.let { flow ->
            val stateFlow = MutableStateFlow(List(TOTAL_STEPS) { StepState.TODO })
            // 수동 업데이트
            stateFlow.also { updateStepStates(stateFlow) }
        }

    private val _stepStatesList = MutableStateFlow(List(TOTAL_STEPS) { if (it == 0) StepState.CURRENT else StepState.TODO })
    val stepStatesList: StateFlow<List<StepState>> = _stepStatesList.asStateFlow()

    private val _canComplete = MutableStateFlow(false)
    val canComplete: StateFlow<Boolean> = _canComplete.asStateFlow()

    fun goToStep(step: Int) {
        if (step in 1..TOTAL_STEPS) {
            _currentStep.value = step
            refreshStates()
        }
    }

    fun nextStep() {
        val current = _currentStep.value
        if (current < TOTAL_STEPS) {
            _currentStep.value = current + 1
            refreshStates()
        }
    }

    fun previousStep() {
        val current = _currentStep.value
        if (current > 1) {
            _currentStep.value = current - 1
            refreshStates()
        }
    }

    fun markStepCompleted(step: Int) {
        _completedSteps.value = _completedSteps.value + step
        refreshStates()
    }

    fun isStepCompleted(step: Int): Boolean = step in _completedSteps.value

    private fun refreshStates() {
        val current = _currentStep.value
        val completed = _completedSteps.value

        _stepStatesList.value = (1..TOTAL_STEPS).map { step ->
            when {
                step in completed -> StepState.DONE
                step == current -> StepState.CURRENT
                else -> StepState.TODO
            }
        }

        _canComplete.value = REQUIRED_STEPS.all { it in completed }
    }

    private fun updateStepStates(stateFlow: MutableStateFlow<List<StepState>>) {
        // initial state already set
    }
}
