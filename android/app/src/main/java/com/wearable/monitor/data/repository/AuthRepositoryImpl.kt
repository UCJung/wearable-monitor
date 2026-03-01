package com.wearable.monitor.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.wearable.monitor.data.remote.WearableApiService
import com.wearable.monitor.data.remote.dto.LoginRequest
import com.wearable.monitor.data.remote.dto.TokenResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: WearableApiService,
    private val encryptedPrefs: SharedPreferences
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_ROLE = "jwt_role"
    }

    override suspend fun login(username: String, password: String): Result<TokenResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == "SUCCESS" && body.data != null) {
                    val role = body.data.role
                    if (role != "PATIENT") {
                        return Result.failure(Exception("앱은 환자 계정만 로그인할 수 있습니다."))
                    }
                    saveToken(body.data.accessToken, body.data.refreshToken)
                    saveRole(role)
                    Result.success(body.data)
                } else {
                    val message = body?.message ?: "로그인에 실패했습니다."
                    Result.failure(Exception(message))
                }
            } else {
                Result.failure(Exception("로그인에 실패했습니다. (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(Exception("서버에 연결할 수 없습니다."))
        }
    }

    override fun saveToken(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun getToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun clearToken() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ROLE)
            .apply()
    }

    override fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    override fun saveRole(role: String) {
        encryptedPrefs.edit()
            .putString(KEY_ROLE, role)
            .apply()
    }

    override fun getRole(): String? {
        return encryptedPrefs.getString(KEY_ROLE, null)
    }
}
