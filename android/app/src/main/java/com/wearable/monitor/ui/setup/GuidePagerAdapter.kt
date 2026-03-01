package com.wearable.monitor.ui.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class GuidePagerAdapter(
    activity: FragmentActivity,
    private val steps: List<GuideStep>
) : FragmentStateAdapter(activity) {

    data class GuideStep(val icon: String, val title: String, val description: String)

    override fun getItemCount(): Int = steps.size

    override fun createFragment(position: Int): Fragment {
        val step = steps[position]
        return GuideFragment.newInstance(step.icon, step.title, step.description)
    }
}
