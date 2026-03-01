package com.wearable.monitor.data.remote.dto

data class BatchUploadRequest(
    val items: List<BiometricUploadItem>
)

data class BiometricUploadItem(
    val itemCode: String,
    val measuredAt: String,       // ISO-8601
    val valueNumeric: Double?,
    val valueText: String?,
    val durationSec: Int?,
    val category: String,
    val unit: String?
)
