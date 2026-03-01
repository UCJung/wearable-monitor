package com.wearable.monitor.data.repository

import com.wearable.monitor.data.local.entity.BiometricEntity

interface BiometricRepository {

    /** Health Connect에서 데이터를 읽어 Room 버퍼에 저장한다. 저장된 건수를 반환. */
    suspend fun collectAndBuffer(): Result<Int>

    /** 서버 미전송 데이터 조회 (최대 500건) */
    suspend fun getPendingData(): List<BiometricEntity>

    /** 서버 전송 완료된 데이터를 동기화 완료로 마킹 */
    suspend fun markAsSynced(ids: List<Long>)

    /** 전송 완료 후 일정 기간 경과한 데이터 삭제 */
    suspend fun cleanOldData(daysToKeep: Int = 7)
}
