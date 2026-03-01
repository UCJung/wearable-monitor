package com.wearable.monitor.data.remote.dto

data class BatchUploadResponse(
    val totalReceived: Int,
    val savedCount: Int,
    val skippedCount: Int
)
