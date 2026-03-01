package com.wearable.monitor.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wearable.monitor.databinding.ActivityLoginBinding
import com.wearable.monitor.ui.setup.GuideActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 자동 로그인: 토큰이 유효하면 바로 GuideActivity로 이동
        if (viewModel.isLoggedIn()) {
            navigateToGuide()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin()
                true
            } else false
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        viewModel.login(username, password)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            binding.errorBanner.visibility = View.GONE
                        }
                        is LoginUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                            binding.errorBanner.visibility = View.GONE
                        }
                        is LoginUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            navigateToGuide()
                        }
                        is LoginUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            binding.errorBanner.visibility = View.VISIBLE
                            binding.tvErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun navigateToGuide() {
        startActivity(Intent(this, GuideActivity::class.java))
        finish()
    }
}
