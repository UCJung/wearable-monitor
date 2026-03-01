package com.wearable.monitor.ui.setup.steps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wearable.monitor.databinding.FragmentStep4Binding
import com.wearable.monitor.ui.setup.SetupWizardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Step4Fragment : Fragment() {

    private var _binding: FragmentStep4Binding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupWizardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOpenSamsungHealth.setOnClickListener {
            openSamsungHealth()
            // 선택 단계이므로 버튼 클릭만으로 완료 처리
            viewModel.markStepCompleted(4)
        }
    }

    private fun openSamsungHealth() {
        val intent = requireContext().packageManager
            .getLaunchIntentForPackage("com.sec.android.app.shealth")
        if (intent != null) {
            startActivity(intent)
        } else {
            val storeIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://details?id=com.sec.android.app.shealth")
            }
            startActivity(storeIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
