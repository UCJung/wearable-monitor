package com.wearable.monitor.ui.setup.steps

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wearable.monitor.R
import com.wearable.monitor.databinding.FragmentStep2Binding
import com.wearable.monitor.ui.setup.SetupWizardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Step2Fragment : Fragment() {

    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupWizardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkWatchConnection()
    }

    override fun onResume() {
        super.onResume()
        checkWatchConnection()
    }

    private fun checkWatchConnection() {
        // 블루투스 연결된 기기 확인 (간이 감지)
        val btManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = btManager?.adapter
        val isConnected = adapter?.isEnabled == true

        if (isConnected) {
            binding.tvWatchStatus.text = getString(R.string.watch_connected)
            binding.tvWatchStatus.setTextColor(resources.getColor(R.color.ok, null))
            binding.statusDot.setBackgroundColor(resources.getColor(R.color.ok, null))
            viewModel.markStepCompleted(2)
        } else {
            binding.tvWatchStatus.text = getString(R.string.watch_disconnected)
            binding.tvWatchStatus.setTextColor(resources.getColor(R.color.warn, null))
            binding.statusDot.setBackgroundColor(resources.getColor(R.color.warn, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
