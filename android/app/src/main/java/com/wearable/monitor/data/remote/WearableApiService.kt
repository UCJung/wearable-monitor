package com.wearable.monitor.data.remote

import com.wearable.monitor.data.remote.dto.ApiResponse
import com.wearable.monitor.data.remote.dto.BatchUploadRequest
import com.wearable.monitor.data.remote.dto.BatchUploadResponse
import com.wearable.monitor.data.remote.dto.LoginRequest
import com.wearable.monitor.data.remote.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WearableApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<TokenResponse>>

    @POST("biometric/batch")
    suspend fun uploadBatch(@Body request: BatchUploadRequest): Response<ApiResponse<BatchUploadResponse>>
}
