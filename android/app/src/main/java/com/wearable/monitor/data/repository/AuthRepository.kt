package com.wearable.monitor.data.repository

import com.wearable.monitor.data.remote.dto.TokenResponse

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<TokenResponse>
    fun saveToken(accessToken: String, refreshToken: String)
    fun getToken(): String?
    fun clearToken()
    fun isLoggedIn(): Boolean
    fun saveRole(role: String)
    fun getRole(): String?
}
