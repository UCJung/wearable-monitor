package com.wearable.monitor.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.wearable.monitor.R
import com.wearable.monitor.databinding.ActivityGuideBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideBinding

    private val guideSteps = listOf(
        GuidePagerAdapter.GuideStep("📱", "", ""),
        GuidePagerAdapter.GuideStep("❤️", "", ""),
        GuidePagerAdapter.GuideStep("🔗", "", ""),
        GuidePagerAdapter.GuideStep("⚙️", "", ""),
        GuidePagerAdapter.GuideStep("🔋", "", "")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 런타임에 문자열 리소스로 채우기
        val filledSteps = listOf(
            GuidePagerAdapter.GuideStep("📱", getString(R.string.guide_step1_title), getString(R.string.guide_step1_desc)),
            GuidePagerAdapter.GuideStep("❤️", getString(R.string.guide_step2_title), getString(R.string.guide_step2_desc)),
            GuidePagerAdapter.GuideStep("🔗", getString(R.string.guide_step3_title), getString(R.string.guide_step3_desc)),
            GuidePagerAdapter.GuideStep("⚙️", getString(R.string.guide_step4_title), getString(R.string.guide_step4_desc)),
            GuidePagerAdapter.GuideStep("🔋", getString(R.string.guide_step5_title), getString(R.string.guide_step5_desc))
        )

        val adapter = GuidePagerAdapter(this, filledSteps)
        binding.viewPager.adapter = adapter

        updatePageIndicator(0)
        updateButtons(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
                updateButtons(position)
            }
        })

        binding.btnPrevious.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current > 0) {
                binding.viewPager.currentItem = current - 1
            }
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < filledSteps.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                // 마지막 단계 → SetupWizardActivity 이동
                startActivity(Intent(this, SetupWizardActivity::class.java))
                finish()
            }
        }
    }

    private fun updatePageIndicator(position: Int) {
        binding.tvPageIndicator.text = "${position + 1} / ${guideSteps.size}"
    }

    private fun updateButtons(position: Int) {
        binding.btnPrevious.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNext.text = if (position == guideSteps.size - 1) {
            getString(R.string.btn_start_setup)
        } else {
            getString(R.string.btn_next)
        }
    }
}
