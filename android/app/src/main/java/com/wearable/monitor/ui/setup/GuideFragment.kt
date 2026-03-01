package com.wearable.monitor.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wearable.monitor.databinding.FragmentGuideStepBinding

class GuideFragment : Fragment() {

    private var _binding: FragmentGuideStepBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ICON = "icon"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"

        fun newInstance(icon: String, title: String, description: String): GuideFragment {
            return GuideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ICON, icon)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, description)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvIcon.text = arguments?.getString(ARG_ICON) ?: ""
        binding.tvTitle.text = arguments?.getString(ARG_TITLE) ?: ""
        binding.tvDescription.text = arguments?.getString(ARG_DESC) ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
