package com.wearable.monitor.data.remote.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: String
)
