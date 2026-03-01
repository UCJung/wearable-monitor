package com.wearable.monitor.data.remote

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) : Interceptor {

    companion object {
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)

        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
