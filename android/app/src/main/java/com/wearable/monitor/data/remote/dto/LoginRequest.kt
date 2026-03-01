package com.wearable.monitor.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String,
    val platform: String = "ANDROID"
)
