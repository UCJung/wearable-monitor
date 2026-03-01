package com.wearable.monitor.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wearable.monitor.R
import com.wearable.monitor.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, DashboardFragment())
                        .commit()
                    true
                }
                R.id.nav_history -> {
                    // TASK-11에서 HistoryFragment 연결
                    true
                }
                R.id.nav_settings -> {
                    // TASK-11에서 SettingsFragment 연결
                    true
                }
                else -> false
            }
        }
    }
}
