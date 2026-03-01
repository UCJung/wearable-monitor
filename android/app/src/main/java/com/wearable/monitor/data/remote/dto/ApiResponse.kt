package com.wearable.monitor.data.remote.dto

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T?
)
